package com.incidentcopilot.service;

import com.incidentcopilot.dao.ChunkDao;
import com.incidentcopilot.model.DocChunk;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RetrievalService {
    private final ChunkDao chunkDao;
    private final EmbeddingService embeddingService;
    @Value("${app.retrieval.alphaVector:0.75}") private double alpha;
    @Value("${app.retrieval.betaKeyword:0.2}") private double beta;
    @Value("${app.retrieval.gammaRecency:0.05}") private double gamma;
    @Value("${app.retrieval.topK:12}") private int topK;

    public RetrievalService(ChunkDao chunkDao, EmbeddingService embeddingService) {
        this.chunkDao = chunkDao;
        this.embeddingService = embeddingService;
    }

    public List<Map<String,Object>> hybridSearch(String query, String service, String env) throws Exception {
        double[] qvec = embeddingService.embed(query);
        List<DocChunk> vecTop = chunkDao.topKByVector(qvec, service, env, Math.max(topK*2, 30));
        List<DocChunk> kwTop = chunkDao.keywordCandidates(query, service, env, topK*2);

        Map<Long, Double> scoreMap = new HashMap<>();

        for (int i=0;i<vecTop.size();i++) {
            DocChunk c = vecTop.get(i);
            double v = (vecTop.size()-i) / (double) vecTop.size();
            scoreMap.merge(c.chunkId(), alpha * v, Double::sum);
        }
        for (int i=0;i<kwTop.size();i++) {
            DocChunk c = kwTop.get(i);
            double k = (kwTop.size()-i) / (double) kwTop.size();
            scoreMap.merge(c.chunkId(), beta * k, Double::sum);
        }

        Map<Long, DocChunk> byId = new HashMap<>();
        vecTop.forEach(c -> byId.put(c.chunkId(), c));
        kwTop.forEach(c -> byId.putIfAbsent(c.chunkId(), c));

        return scoreMap.entrySet().stream()
                .sorted((a,b)->Double.compare(b.getValue(), a.getValue()))
                .limit(topK)
                .map(e -> {
                    DocChunk c = byId.get(e.getKey());
                    Map<String,Object> m = new LinkedHashMap<>();
                    m.put("chunk_id", c.chunkId());
                    m.put("doc_id", c.docId());
                    m.put("ord", c.ord());
                    m.put("service", c.service());
                    m.put("env", c.env());
                    m.put("content", c.content());
                    m.put("score", e.getValue());
                    return m;
                }).collect(Collectors.toList());
    }
}
