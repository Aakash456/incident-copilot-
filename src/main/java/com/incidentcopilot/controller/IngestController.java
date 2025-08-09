package com.incidentcopilot.controller;

import com.incidentcopilot.dao.DocDao;
import com.incidentcopilot.dao.ChunkDao;
import com.incidentcopilot.service.ChunkerService;
import com.incidentcopilot.service.EmbeddingService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

@RestController
@RequestMapping("/ingest")
public class IngestController {
    private final DocDao docDao;
    private final ChunkDao chunkDao;
    private final ChunkerService chunker = new ChunkerService();
    private final EmbeddingService embeddingService;

    public IngestController(DocDao docDao, ChunkDao chunkDao, EmbeddingService embeddingService) {
        this.docDao = docDao;
        this.chunkDao = chunkDao;
        this.embeddingService = embeddingService;
    }

    public record IngestReq(String url, String text, List<String> tags, String service, String env) { }
    public record IngestResp(long docId, int chunks) { }

    @PostMapping(value="/docs", consumes = MediaType.APPLICATION_JSON_VALUE)
    public IngestResp ingest(@RequestBody IngestReq req) throws Exception {
        String content = req.text();
        String title = "ad hoc";
        if (content == null && req.url()!=null) {
            var http = HttpClient.newHttpClient();
            var r = http.send(HttpRequest.newBuilder().uri(URI.create(req.url())).GET().build(), HttpResponse.BodyHandlers.ofString());
            content = r.body();
            title = req.url();
        }
        if (content == null || content.isBlank()) throw new IllegalArgumentException("Provide 'url' or 'text'");
        long docId = docDao.insert(title, req.url());
        var chunks = chunker.chunk(content, 3600, 400);
        int ord = 0;
        for (var ch : chunks) {
            double[] vec = embeddingService.embed(ch);
            chunkDao.insert(docId, ord++, ch, vec, Math.min(ch.length()/4, 1200), req.service(), req.env());
        }
        return new IngestResp(docId, chunks.size());
    }
}
