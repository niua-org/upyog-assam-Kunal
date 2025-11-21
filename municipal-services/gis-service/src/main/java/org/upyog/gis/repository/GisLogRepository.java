package org.upyog.gis.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.upyog.gis.kafka.Producer;
import org.upyog.gis.model.GisLog;
import org.upyog.gis.model.GisLogSearchCriteria;
import org.upyog.gis.repository.querybuilder.GisQueryBuilder;
import org.upyog.gis.repository.rowmapper.GisRowMapper;

import java.util.ArrayList;
import java.util.List;

/**
 * Repository for GisLog entity that publishes logs to Kafka.
 * 
 * <p>This repository handles the publishing of GIS processing logs to Kafka topics
 * for consumption by other services in the ecosystem using the standard Producer pattern.</p>
 * 
 * @author GIS Service Team
 * @version 1.0
 * @since 1.0
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class GisLogRepository {

    private final Producer producer;
    private final JdbcTemplate jdbcTemplate;
    private final GisQueryBuilder queryBuilder;
    private final GisRowMapper rowMapper;
    
    private static final String GIS_LOG_TOPIC = "save-gis-log";

    /**
     * Publishes a GIS log event to Kafka.
     * 
     * @param gisLog the GIS log object to publish
     */
    public void save(GisLog gisLog) {
        try {
            log.info("Publishing GIS log to Kafka topic: {} for application: {}", 
                    GIS_LOG_TOPIC, gisLog.getApplicationNo());
            
            producer.push(GIS_LOG_TOPIC, gisLog);
            
            log.debug("Successfully published GIS log with ID: {}", gisLog.getId());
        } catch (Exception e) {
            log.error("Failed to publish GIS log to Kafka: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to publish GIS log to Kafka", e);
        }
    }

    public List<GisLog> search(GisLogSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getGisLogSearchQuery(criteria, preparedStmtList);

        log.info("Executing GIS log search query with criteria: {}", criteria);
        log.debug("Query: {}", query);

        return jdbcTemplate.query(query, preparedStmtList.toArray(), rowMapper);
    }

    public Integer count(GisLogSearchCriteria criteria) {
        List<Object> preparedStmtList = new ArrayList<>();
        String query = queryBuilder.getCountQuery(criteria, preparedStmtList);

        log.debug("Executing count query: {}", query);

        return jdbcTemplate.queryForObject(query, preparedStmtList.toArray(), Integer.class);
    }
}
