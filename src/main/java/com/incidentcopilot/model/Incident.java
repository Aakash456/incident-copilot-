package com.incidentcopilot.model;

import java.time.Instant;
import com.incidentcopilot.model.Enums.*;

public record Incident(Long incidentId, Instant createdAt, Status status, Severity severity, String title,
                       String summaryMd, String suspectedCause, String service, String env, double confidence) {}
