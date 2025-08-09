package com.incidentcopilot.model;

import java.time.Instant;

public record LogEntry(Long logId, Instant ts, String service, String env, String level, String message) {}
