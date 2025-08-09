package com.incidentcopilot.dao;

import com.incidentcopilot.model.Document;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class DocDao {
    private final JdbcTemplate jdbc;

    public DocDao(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public long insert(String title, String sourceUrl) {
        jdbc.update("INSERT INTO documents(title, source_url) VALUES (?,?)", title, sourceUrl);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public List<Document> listRecent(int limit) {
        return jdbc.query("SELECT doc_id,title,source_url,created_at FROM documents ORDER BY created_at DESC LIMIT ?",
                (rs, i) -> new Document(rs.getLong(1), rs.getString(2), rs.getString(3), rs.getTimestamp(4).toInstant()), limit);
    }
}
