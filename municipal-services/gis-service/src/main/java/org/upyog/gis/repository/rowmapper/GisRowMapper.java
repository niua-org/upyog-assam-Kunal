package org.upyog.gis.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import org.upyog.gis.model.GisLog;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class GisRowMapper implements ResultSetExtractor<List<GisLog>> {

    private final ObjectMapper objectMapper;

    @Override
    public List<GisLog> extractData(ResultSet rs) throws SQLException {
        List<GisLog> gisLogs = new ArrayList<>();

        while (rs.next()) {
            GisLog gisLog = GisLog.builder()
                    .id(rs.getString("id"))
                    .applicationNo(rs.getString("application_no"))
                    .rtpiId(rs.getString("rtpi_id"))
                    .fileStoreId(rs.getString("file_store_id"))
                    .latitude(rs.getDouble("latitude"))
                    .longitude(rs.getDouble("longitude"))
                    .tenantId(rs.getString("tenant_id"))
                    .status(rs.getString("status"))
                    .responseStatus(rs.getString("response_status"))
                    .responseJson(rs.getString("response_json"))
                    .createdby(rs.getString("createdby"))
                    .createdtime(rs.getLong("createdtime"))
                    .lastmodifiedby(rs.getString("lastmodifiedby"))
                    .lastmodifiedtime(rs.getObject("lastmodifiedtime") != null ? rs.getLong("lastmodifiedtime") : null)
                    .details(parseDetails(rs))
                    .build();

            gisLogs.add(gisLog);
        }

        return gisLogs;
    }

    private JsonNode parseDetails(ResultSet rs) throws SQLException {
        try {
            String detailsJson = rs.getString("details");
            if (detailsJson != null && !detailsJson.isEmpty()) {
                return objectMapper.readTree(detailsJson);
            }
        } catch (Exception e) {
            log.error("Failed to parse details JSONB", e);
        }
        return null;
    }
}
