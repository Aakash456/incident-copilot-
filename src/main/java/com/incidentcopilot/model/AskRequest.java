package com.incidentcopilot.model;

import jakarta.validation.constraints.NotBlank;

public record AskRequest(
        @NotBlank String question,
        AskRequestContext context
) {}
