package com.incidentcopilot.service;

import java.util.ArrayList;
import java.util.List;

public class ChunkerService {
    // Simple token-approx chunker by characters (~900 tokens =~ 3600 chars)
    public List<String> chunk(String text, int maxChars, int overlap) {
        List<String> out = new ArrayList<>();
        int i = 0;
        while (i < text.length()) {
            int end = Math.min(text.length(), i + maxChars);
            String chunk = text.substring(i, end);
            out.add(chunk);
            if (end == text.length()) break;
            i = Math.max(0, end - overlap);
        }
        return out;
    }
}
