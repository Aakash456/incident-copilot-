package com.incidentcopilot.model;

public record DocChunk(Long chunkId, Long docId, int ord, String content, double[] embedding, int tokens, String service, String env) {}
