package org.upyog.gis.repository.querybuilder;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.upyog.gis.model.GisLogSearchCriteria;

import java.util.ArrayList;
import java.util.List;

@Component
public class GisQueryBuilder {

    private static final String BASE_SEARCH_QUERY = "SELECT " +
            "id, application_no, rtpi_id, file_store_id, latitude, longitude, " +
            "tenant_id, status, response_status, response_json, createdby, createdtime, " +
            "lastmodifiedby, lastmodifiedtime, details " +
            "FROM ug_gis_log";

    public String getGisLogSearchQuery(GisLogSearchCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder(BASE_SEARCH_QUERY);
        List<String> conditions = new ArrayList<>();

        if (criteria != null) {
            if (StringUtils.hasText(criteria.getTenantId())) {
                conditions.add("tenant_id = ?");
                preparedStmtList.add(criteria.getTenantId());
            }

            if (StringUtils.hasText(criteria.getApplicationNo())) {
                conditions.add("application_no = ?");
                preparedStmtList.add(criteria.getApplicationNo());
            }

            if (StringUtils.hasText(criteria.getRtpId())) {
                conditions.add("rtpi_id = ?");
                preparedStmtList.add(criteria.getRtpId());
            }

            if (StringUtils.hasText(criteria.getStatus())) {
                conditions.add("status = ?");
                preparedStmtList.add(criteria.getStatus());
            }
        }

        if (!conditions.isEmpty()) {
            query.append(" WHERE ");
            query.append(String.join(" AND ", conditions));
        }

        query.append(" ORDER BY createdtime DESC");

        // Add pagination
        if (criteria != null && criteria.getLimit() != null) {
            query.append(" LIMIT ?");
            preparedStmtList.add(criteria.getLimit());

            if (criteria.getOffset() != null) {
                query.append(" OFFSET ?");
                preparedStmtList.add(criteria.getOffset());
            }
        }

        return query.toString();
    }

    public String getCountQuery(GisLogSearchCriteria criteria, List<Object> preparedStmtList) {
        StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM ug_gis_log");
        List<String> conditions = new ArrayList<>();

        if (criteria != null) {
            if (StringUtils.hasText(criteria.getTenantId())) {
                conditions.add("tenant_id = ?");
                preparedStmtList.add(criteria.getTenantId());
            }

            if (StringUtils.hasText(criteria.getApplicationNo())) {
                conditions.add("application_no = ?");
                preparedStmtList.add(criteria.getApplicationNo());
            }

            if (StringUtils.hasText(criteria.getRtpId())) {
                conditions.add("rtpi_id = ?");
                preparedStmtList.add(criteria.getRtpId());
            }

            if (StringUtils.hasText(criteria.getStatus())) {
                conditions.add("status = ?");
                preparedStmtList.add(criteria.getStatus());
            }
        }

        if (!conditions.isEmpty()) {
            query.append(" WHERE ");
            query.append(String.join(" AND ", conditions));
        }

        return query.toString();
    }
}
