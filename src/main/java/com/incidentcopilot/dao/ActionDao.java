package com.incidentcopilot.dao;

import com.incidentcopilot.model.ActionItem;
import com.incidentcopilot.model.Enums;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ActionDao {
    private final JdbcTemplate jdbc;
    public ActionDao(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    public long insert(Long incidentId, Enums.ActionKind kind, String payloadJson, String externalRef) {
        jdbc.update("INSERT INTO actions (incident_id, kind, payload_json, external_ref) VALUES (?,?,CAST(? AS JSON),?)",
                incidentId, kind.name(), payloadJson, externalRef);
        return jdbc.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    public void setStatus(long id, String status, String externalRef) {
        jdbc.update("UPDATE actions SET status=?, external_ref=? WHERE action_id=?", status, externalRef, id);
    }

    public List<ActionItem> listByIncident(long incidentId) {
        return jdbc.query("SELECT action_id, incident_id, kind, JSON_EXTRACT(payload_json, '$') as pj, status, external_ref FROM actions WHERE incident_id=?",
                (rs,i) -> new ActionItem(rs.getLong(1), rs.getLong(2),
                        Enums.ActionKind.valueOf(rs.getString(3)), rs.getString(4), rs.getString(5), rs.getString(6)), incidentId);
    }
}
