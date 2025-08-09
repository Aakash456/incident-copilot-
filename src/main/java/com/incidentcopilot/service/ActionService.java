package com.incidentcopilot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.incidentcopilot.dao.ActionDao;
import com.incidentcopilot.model.Enums;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class ActionService {
    private final SlackService slack;
    private final GitHubService gh;
    private final ActionDao actionDao;
    private final ObjectMapper om = new ObjectMapper();

    public ActionService(SlackService slack, GitHubService gh, ActionDao actionDao) {
        this.slack = slack;
        this.gh = gh;
        this.actionDao = actionDao;
    }

    public List<Map<String,Object>> apply(List<Map<String,Object>> actions, Long incidentId) throws Exception {
        for (Map<String,Object> a : actions) {
            String kind = a.get("kind").toString();
            Map<String,Object> payload = (Map<String, Object>) a.get("payload");
            String ref = null;
            long id = actionDao.insert(incidentId, Enums.ActionKind.valueOf(kind), om.writeValueAsString(payload), null);
            try {
                switch (kind) {
                    case "SLACK" -> {
                        ref = slack.postMessage(payload.getOrDefault("text","(empty)").toString());
                        actionDao.setStatus(id, "DONE", ref);
                    }
                    case "GITHUB" -> {
                        ref = gh.createIssue(payload.getOrDefault("title","Incident").toString(),
                                payload.getOrDefault("body","Created by Incident Copilot").toString());
                        actionDao.setStatus(id, "DONE", ref);
                    }
                    default -> actionDao.setStatus(id, "FAILED", "unsupported");
                }
            } catch (Exception ex) {
                actionDao.setStatus(id, "FAILED", (ref!=null?ref:"") + " " + ex.getMessage());
                throw ex;
            }
        }
        return actions;
    }
}
