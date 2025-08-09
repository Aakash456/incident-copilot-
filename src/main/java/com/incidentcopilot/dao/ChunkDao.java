package com.incidentcopilot.dao;

import com.incidentcopilot.model.DocChunk;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ChunkDao {
    private final JdbcTemplate jdbc;

    public ChunkDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public void insert(long docId, int ord, String content, double[] embedding, int tokens, String service, String env) {
        String vecCsv = java.util.Arrays.stream(embedding).mapToObj(Double::toString).collect(java.util.stream.Collectors.joining(","));
        // Use VECTOR(...) literal for TiDB's VECTOR type
        String embedLiteral = "VECTOR(" + vecCsv + ")";
        String sql = "INSERT INTO doc_chunks (doc_id,ord,content,embedding,tokens,service,env) VALUES ("+
                docId + "," + ord + ",?," + embedLiteral + "," + tokens + ",?,?)";
        jdbc.update(sql, content, service, env);
    }

    public List<DocChunk> topKByVector(double[] query, String service, String env, int k) {
        String queryCsv = java.util.Arrays.stream(query).mapToObj(Double::toString).collect(java.util.stream.Collectors.joining(","));
        String sql = "SELECT chunk_id, doc_id, ord, content, tokens, service, env, " +
                     "DOT_PRODUCT(embedding, TO_VECTOR(?)) AS vscore " +
                     "FROM doc_chunks " +
                     "WHERE (? IS NULL OR service = ?) AND (? IS NULL OR env = ?) " +
                     "ORDER BY vscore DESC LIMIT ?";
        return jdbc.query(sql,
                ps -> {
                    ps.setString(1, queryCsv);
                    ps.setString(2, service); ps.setString(3, service);
                    ps.setString(4, env); ps.setString(5, env);
                    ps.setInt(6, k);
                },
                (rs,i)-> new DocChunk(
                        rs.getLong("chunk_id"),
                        rs.getLong("doc_id"),
                        rs.getInt("ord"),
                        rs.getString("content"),
                        new double[0],
                        rs.getInt("tokens"),
                        rs.getString("service"),
                        rs.getString("env")
                ));
    }

    public List<DocChunk> keywordCandidates(String q, String service, String env, int k) {
        String like = "%" + q + "%";
        String sql = "SELECT chunk_id, doc_id, ord, content, tokens, service, env " +
                "FROM doc_chunks " +
                "WHERE content LIKE ? " +
                "AND (? IS NULL OR service = ?) " +
                "AND (? IS NULL OR env = ?) " +
                "ORDER BY created_at DESC LIMIT ?";
        return jdbc.query(sql, ps -> {
            ps.setString(1, like);
            ps.setString(2, service); ps.setString(3, service);
            ps.setString(4, env); ps.setString(5, env);
            ps.setInt(6, k);
        }, (rs,i)-> new DocChunk(
                rs.getLong("chunk_id"),
                rs.getLong("doc_id"),
                rs.getInt("ord"),
                rs.getString("content"),
                new double[0],
                rs.getInt("tokens"),
                rs.getString("service"),
                rs.getString("env")
        ));
    }
}
