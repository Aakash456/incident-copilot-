package com.incidentcopilot.controller;

import com.incidentcopilot.model.AskRequest;
import com.incidentcopilot.model.AskResponse;
import com.incidentcopilot.service.OrchestratorService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
public class AskController {
    private final OrchestratorService orchestrator;

    public AskController(OrchestratorService orchestrator) {
        this.orchestrator = orchestrator;
    }

    @PostMapping("/ask")
    public AskResponse ask(@Valid @RequestBody AskRequest req) throws Exception {
        Map<String,Object> out = orchestrator.handleAsk(req.question(),
                req.context()==null?null:req.context().service(),
                req.context()==null?null:req.context().env());
        return new AskResponse(out.get("answer").toString(),
                (Double) out.get("confidence"),
                (List<Map<String,Object>>) out.get("sources"),
                (List<Map<String,Object>>) out.get("actions"));
    }
}
