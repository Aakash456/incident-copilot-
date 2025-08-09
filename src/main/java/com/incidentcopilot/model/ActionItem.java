package com.incidentcopilot.model;

import com.incidentcopilot.model.Enums.ActionKind;

public record ActionItem(Long actionId, Long incidentId, ActionKind kind, String payloadJson, String status, String externalRef) {}
