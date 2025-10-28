package org.egov.bpa.repository.rowmapper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.egov.bpa.web.model.*;
import org.postgresql.util.PGobject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Component
@Slf4j
public class BPARowMapper implements ResultSetExtractor<List<BPA>> {

	@Autowired
	private ObjectMapper mapper;

	@Override
	public List<BPA> extractData(ResultSet rs) throws SQLException, DataAccessException {
		Map<String, BPA> buildingMap = new LinkedHashMap<>();

		while (rs.next()) {
			String id = rs.getString("bpa_id");
			BPA currentbpa = buildingMap.get(id);

			if (currentbpa == null) {
				currentbpa = buildBpa(rs);
				buildingMap.put(id, currentbpa);
			}

			// Attach child entities
			addDocument(rs, currentbpa);
			addRtpDetail(rs, currentbpa);
			addAreaMappingDetail(rs, currentbpa);
		}

		return new ArrayList<>(buildingMap.values());
	}

	/** ---------------------- BPA CORE MAPPING ---------------------- */
	private BPA buildBpa(ResultSet rs) throws SQLException {
		Long lastModifiedTime = rs.getLong("bpa_last_modified_time");
		if (rs.wasNull()) lastModifiedTime = null;

		Object additionalDetails = new Gson().fromJson(
				(rs.getString("additional_details").equals("{}") || rs.getString("additional_details").equals("null"))
						? null
						: rs.getString("additional_details"),
				Object.class);

		AuditDetails auditdetails = AuditDetails.builder()
				.createdBy(rs.getString("bpa_created_by"))
				.createdTime(rs.getLong("bpa_created_time"))
				.lastModifiedBy(rs.getString("bpa_last_modified_by"))
				.lastModifiedTime(lastModifiedTime)
				.build();

		JsonObject jsonObject = new Gson().fromJson(
				(rs.getString("additional_details").equals("{}") || rs.getString("additional_details").equals("null"))
						? "{}"
						: rs.getString("additional_details"),
				JsonObject.class
		);

		String riskType = null;
		if (jsonObject.has("risk_type")) {
			JsonElement riskTypeElement = jsonObject.get("risk_type");
			if (riskTypeElement != null && !riskTypeElement.isJsonNull()) {
				riskType = riskTypeElement.getAsString();
			}
		}

		return BPA.builder()
				.id(rs.getString("bpa_id"))
				.applicationNo(rs.getString("application_no"))
				.approvalNo(rs.getString("approval_no"))
				.status(rs.getString("status"))
				.tenantId(rs.getString("bpa_tenant_id"))
				.edcrNumber(rs.getString("edcr_number"))
				.approvalDate(rs.getLong("approval_date"))
				.accountId(rs.getString("account_id"))
				.landId(rs.getString("land_id"))
				.applicationDate(rs.getLong("application_date"))
				.auditDetails(auditdetails)
				.additionalDetails(additionalDetails)
				.businessService(rs.getString("business_service"))
				.riskType(riskType)
				.build();
	}

	/** ---------------------- DOCUMENT MAPPING ---------------------- */
	private void addDocument(ResultSet rs, BPA bpa) throws SQLException {
		String documentId = rs.getString("bpa_doc_id");
		if (documentId == null) return;

		Object docDetails = null;
		if (rs.getString("doc_details") != null) {
			docDetails = new Gson().fromJson(
					(rs.getString("doc_details").equals("{}") || rs.getString("doc_details").equals("null"))
							? null
							: rs.getString("doc_details"),
					Object.class
			);
		}

		Document document = Document.builder()
				.id(documentId)
				.documentType(rs.getString("bpa_doc_document_type"))
				.fileStoreId(rs.getString("bpa_doc_filestore"))
				.documentUid(rs.getString("document_uid"))
				.additionalDetails(docDetails)
				.build();

		bpa.addDocument(document);
	}

	/** ---------------------- RTP DETAIL MAPPING ---------------------- */
	private void addRtpDetail(ResultSet rs, BPA bpa) throws SQLException {
		String rtpId = rs.getString("rtp_id");
		if (rtpId == null) return;

		Object rtpAdditionalDetails = null;
		PGobject rtpPgObj = (PGobject) rs.getObject("rtp_additional_details");
		if (rtpPgObj != null && rtpPgObj.getValue() != null) {
			try {
				rtpAdditionalDetails = mapper.readTree(rtpPgObj.getValue());
			} catch (IOException e) {
				log.error("Failed to parse additionalDetails for RTP", e);
			}
		}

		RTPAllocationDetails rtpDetail = RTPAllocationDetails.builder()
				.id(rs.getString("id"))
				.applicationId(rs.getString("buildingplan_id"))
				.rtpUUID(rs.getString("rtp_id"))
				.rtpCategory(RTPAllocationDetails.RTPCategory.valueOf(rs.getString("rtp_category")))
				.rtpName(rs.getString("rtp_name"))
				.assignmentStatus(rs.getString("rtp_assignment_status"))
				.assignmentDate(rs.getLong("rtp_assignment_date"))
				.changedDate(rs.getLong("rtp_changed_date"))
				.remarks(rs.getString("rtp_remarks"))
				.additionalDetails(rtpAdditionalDetails)
				.build();

		bpa.setRtpDetails(rtpDetail);
	}

	/** ---------------------- AREA MAPPING DETAIL ---------------------- */
	private void addAreaMappingDetail(ResultSet rs, BPA bpa) throws SQLException {
		String areaId = rs.getString("area_id");
		if (areaId == null) return;

		AreaMappingDetail areaMappingDetail = new AreaMappingDetail();
		areaMappingDetail.setId(areaId);
		areaMappingDetail.setDistrict(rs.getString("area_district"));
		areaMappingDetail.setPlanningArea(rs.getString("area_planning_area"));

		String planningAuthority = rs.getString("area_planning_permit_authority");
		if (planningAuthority != null) {
			areaMappingDetail.setPlanningPermitAuthority(
					PlanningPermitAuthorityEnum.valueOf(planningAuthority));
		}

		String buildingAuthority = rs.getString("area_building_permit_authority");
		if (buildingAuthority != null) {
			areaMappingDetail.setBuildingPermitAuthority(
					BuildingPermitAuthorityEnum.valueOf(buildingAuthority));
		}

		areaMappingDetail.setRevenueVillage(rs.getString("area_revenue_village"));
		areaMappingDetail.setVillageName(rs.getString("area_village_name"));
		areaMappingDetail.setConcernedAuthority(rs.getString("area_concerned_authority"));
		areaMappingDetail.setMouza(rs.getString("area_mouza"));
		areaMappingDetail.setWard(rs.getString("area_ward"));

		bpa.setAreaMapping(areaMappingDetail);
	}
}
