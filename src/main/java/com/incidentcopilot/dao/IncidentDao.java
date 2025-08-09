package com.incidentcopilot.dao;

import com.incidentcopilot.model.Enums;
import com.incidentcopilot.model.Incident;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class IncidentDao {
    private final JdbcTemplate jdbc;
    public IncidentDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public long insert(String title, String summaryMd, String suspectedCause, String service, String env, Enums.Severity sev, double confidence) {
        jdbc.update("INSERT INTO incidents (title, summary_md, suspected_cause, service, env, severity, confidence) VALUES (?,?,?,?,?,?,?)",
                title, summaryMd, suspectedCause, service, env, sev.name(), confidence);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public Incident get(long id) {
        return jdbc.queryForObject("SELECT incident_id, created_at, status, severity, title, summary_md, suspected_cause, service, env, confidence FROM incidents WHERE incident_id=?",
                (rs,i) -> new Incident(rs.getLong(1), rs.getTimestamp(2).toInstant(),
                        Enums.Status.valueOf(rs.getString(3)), Enums.Severity.valueOf(rs.getString(4)),
                        rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getDouble(10)), id);
    }

    public List<Incident> list(int limit) {
        return jdbc.query("SELECT incident_id, created_at, status, severity, title, summary_md, suspected_cause, service, env, confidence FROM incidents ORDER BY created_at DESC LIMIT ?",
                (rs,i) -> new Incident(rs.getLong(1), rs.getTimestamp(2).toInstant(),
                        Enums.Status.valueOf(rs.getString(3)), Enums.Severity.valueOf(rs.getString(4)),
                        rs.getString(5), rs.getString(6), rs.getString(7), rs.getString(8), rs.getString(9), rs.getDouble(10)), limit);
    }
}
