package com.incidentcopilot.model;

import java.time.Instant;

public record Document(Long docId, String title, String sourceUrl, Instant createdAt) {}
