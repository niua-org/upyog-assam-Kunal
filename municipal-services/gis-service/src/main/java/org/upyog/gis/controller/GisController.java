package org.upyog.gis.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.egov.common.contract.request.RequestInfo;
import org.egov.common.contract.response.ResponseInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.upyog.gis.model.*;
import org.upyog.gis.service.GisService;

import java.util.List;

/**
 * REST controller for GIS operations
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@Api(value = "GIS Controller", description = "Operations for GIS processing")
public class GisController {

    private final GisService gisService;

    /**
     * Find zone information from polygon file
     *
     * @param file the polygon file to process
     * @param gisRequestWrapper the GIS request containing tenant id, application number, RTPI ID and RequestInfo
     * @return response containing district, zone, and WFS response
     */
    @ApiOperation(value = "Find zone from polygon file", notes = "Uploads and processes a polygon KML file to find zone information")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    @PostMapping(value = "/find-zone", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<GISResponse> findZone(
            @ApiParam(value = "Polygon KML file", required = true)
            @RequestPart("file") MultipartFile file,
            @ApiParam(value = "GIS Request with RequestInfo", required = true)
            @RequestPart(value = "gisRequestWrapper") GISRequestWrapper gisRequestWrapper
    ) {

        try {
            log.info("Finding zone from polygon file: {} (tenant: {}, applicationNo: {}, rtpiId: {})", 
                    file.getOriginalFilename(), gisRequestWrapper.getGisRequest().getTenantId(), 
                    gisRequestWrapper.getGisRequest().getApplicationNo(), gisRequestWrapper.getGisRequest().getRtpiId());

            GISResponse response = gisService.findZoneFromGeometry(file, gisRequestWrapper);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            GISResponse errorResponse = GISResponse.builder()
                    .error(e.getMessage())
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            log.error("Error finding zone from polygon file", e);
            GISResponse errorResponse = GISResponse.builder()
                    .error("Internal server error: " + e.getMessage())
                    .build();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    @ApiOperation(value = "Search GIS logs", notes = "Search GIS processing logs based on criteria")
    @ApiResponses({
            @ApiResponse(code = 200, message = "OK")
    })
    @PostMapping("/zone/_search")
    public ResponseEntity<GisLogSearchResponse> searchGisLogs(
            @ApiParam(value = "Search criteria", required = true)
            @RequestBody GisLogSearchRequest searchRequest
    ) {
        try {
            log.info("Searching GIS logs with criteria: {}", searchRequest.getCriteria());

            List<GisLog> gisLogs = gisService.searchGisLog(searchRequest.getCriteria());

            GisLogSearchResponse response = GisLogSearchResponse.builder()
                    .responseInfo(createResponseInfo(searchRequest.getRequestInfo()))
                    .gisLogs(gisLogs)
                    .build();

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid search request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Error searching GIS logs", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    private ResponseInfo createResponseInfo(RequestInfo requestInfo) {
        return ResponseInfo.builder()
                .apiId(requestInfo != null ? requestInfo.getApiId() : null)
                .ver(requestInfo != null ? requestInfo.getVer() : null)
                .ts(System.currentTimeMillis())
                .resMsgId("uief87324")
                .msgId(requestInfo != null ? requestInfo.getMsgId() : null)
                .status("successful")
                .build();
    }
}
