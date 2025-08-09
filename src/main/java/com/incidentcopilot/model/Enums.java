package com.incidentcopilot.model;

public class Enums {
    public enum Severity { SEV1, SEV2, SEV3 }
    public enum Status { OPEN, ACK, RESOLVED }
    public enum ActionKind { SLACK, GITHUB, JIRA, CALENDAR }
}
