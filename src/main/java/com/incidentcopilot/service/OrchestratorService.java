package com.incidentcopilot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidentcopilot.dao.ActionDao;
import com.incidentcopilot.dao.IncidentDao;
import com.incidentcopilot.model.Enums;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class OrchestratorService {
    private final RetrievalService retrieval;
    private final LlmService llm;
    private final IncidentDao incidentDao;
    private final ActionDao actionDao;
    private final ObjectMapper om = new ObjectMapper();

    public OrchestratorService(RetrievalService retrieval, LlmService llm, IncidentDao incidentDao, ActionDao actionDao) {
        this.retrieval = retrieval;
        this.llm = llm;
        this.incidentDao = incidentDao;
        this.actionDao = actionDao;
    }

    public Map<String,Object> handleAsk(String question, String service, String env) throws Exception {
        var sources = retrieval.hybridSearch(question, service, env);
        String context = buildContextForLLM(sources);
        String system = "You are Incident Copilot. Given a question and context, " +
                "classify severity (SEV1/SEV2/SEV3), summarize likely root cause, " +
                "and propose actions as JSON for Slack and GitHub only. Always return fields: summary, suspected_cause, severity, confidence, actions.";
        String user = "Question: " + question + "\n\nContext:\n" + context;

        Map<String,Object> toolsSpec = Map.of("actions", List.of(
                Map.of("kind","SLACK","payload", Map.of("text","string")),
                Map.of("kind","GITHUB","payload", Map.of("title","string","body","string"))
        ));

        JsonNode out = llm.reason(system, user, toolsSpec);
        String summary = out.path("summary").asText("");
        String suspected = out.path("suspected_cause").asText("");
        String sev = out.path("severity").asText("SEV3");
        double confidence = out.path("confidence").asDouble(0.7);
        List<Map<String,Object>> actions = new ArrayList<>();
        if (out.has("actions") && out.get("actions").isArray()) {
            for (JsonNode a : out.get("actions")) {
                Map<String,Object> m = new LinkedHashMap<>();
                m.put("kind", a.path("kind").asText("SLACK"));
                m.put("payload", new ObjectMapper().convertValue(a.path("payload"), Map.class));
                actions.add(m);
            }
        }

        long incidentId = incidentDao.insert(
                truncate("Incident: " + question, 240),
                summary, suspected, service, env,
                Enums.Severity.valueOf(sev.toUpperCase()), confidence);

        // Persist planned actions with PENDING status
        for (Map<String,Object> a : actions) {
            String payloadJson = om.writeValueAsString(a.get("payload"));
            actionDao.insert(incidentId, Enums.ActionKind.valueOf(a.get("kind").toString()), payloadJson, null);
        }

        Map<String,Object> resp = new LinkedHashMap<>();
        resp.put("answer", summary);
        resp.put("confidence", confidence);
        resp.put("sources", sources);
        resp.put("actions", actions);
        resp.put("incident_id", incidentId);
        return resp;
    }

    private String buildContextForLLM(List<Map<String,Object>> sources) {
        StringBuilder sb = new StringBuilder();
        for (var s : sources) {
            sb.append("- [doc ").append(s.get("doc_id")).append(" #").append(s.get("ord")).append("] ")
              .append(s.get("content")).append("\n");
            if (sb.length() > 8000) break;
        }
        return sb.toString();
    }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max);
    }
}
