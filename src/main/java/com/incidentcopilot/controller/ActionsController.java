package com.incidentcopilot.controller;

import com.incidentcopilot.service.ActionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/actions")
public class ActionsController {
    private final ActionService actions;

    public ActionsController(ActionService actions) { this.actions = actions; }

    public record ApplyReq(List<Map<String,Object>> actions, Long incidentId){}
    public record ApplyResp(List<Map<String,Object>> applied){}

    @PostMapping("/apply")
    public ApplyResp apply(@RequestBody ApplyReq req) throws Exception {
        return new ApplyResp(actions.apply(req.actions, req.incidentId));
    }
}
