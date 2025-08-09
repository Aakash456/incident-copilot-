package com.incidentcopilot.model;

import java.util.List;
import java.util.Map;

public record AskResponse(String answer, double confidence, List<Map<String,Object>> sources, List<Map<String,Object>> actions) {}
