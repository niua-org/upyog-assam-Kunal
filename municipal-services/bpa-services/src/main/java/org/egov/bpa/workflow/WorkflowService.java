package org.egov.bpa.workflow;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.repository.ServiceRequestRepository;
import org.egov.bpa.util.BPAConstants;
import org.egov.bpa.util.BPAErrorConstants;
import org.egov.bpa.web.model.*;
import org.egov.bpa.web.model.workflow.BusinessService;
import org.egov.bpa.web.model.workflow.BusinessServiceResponse;
import org.egov.bpa.web.model.workflow.State;
import org.egov.common.contract.request.RequestInfo;
import org.egov.tracer.model.CustomException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class WorkflowService {

	private BPAConfiguration config;

	private ServiceRequestRepository serviceRequestRepository;

	private ObjectMapper mapper;


	// Custom key class for (Planning, Building) combination
	@AllArgsConstructor
	@EqualsAndHashCode
	private static class AuthorityKey {
		private final PlanningPermitAuthorityEnum planning;
		private final BuildingPermitAuthorityEnum building;
	}


	// Map from AuthorityKey -> Business Service String
	private static final Map<AuthorityKey, String> BUSINESS_SERVICE_MAP = new HashMap<>();
	static {
		// Generic mappings
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DA, BuildingPermitAuthorityEnum.MB), "BPA_DA_MB");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TACP, BuildingPermitAuthorityEnum.GP), "BPA_TACP_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.GMDA, BuildingPermitAuthorityEnum.GMC), "BPA_GMDA_GMC");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.GMDA, BuildingPermitAuthorityEnum.NGMB), "BPA_GMDA_NGMB");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.GMDA, BuildingPermitAuthorityEnum.GP), "BPA_GMDA_GP");

		// District-specific Development Authorities
		// ULB → BPA_DA_MB
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.NALBARI_MB), "BPA_DA_MB");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.BARPETA_DA, BuildingPermitAuthorityEnum.BARPETA_MB), "BPA_DA_MB");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.DIBRUGARH_MC), "BPA_DA_MB");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.TINSUKIA_MB), "BPA_DA_MB");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.SILCHAR_MC), "BPA_DA_MB");

		// Gram Panchayats → BPA_DA_GP
		// Nalbari GP
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.PUB_BAHJANI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.BALITARA_BATAHGILA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.SARIATOLI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.CHANDAKUCHI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.UTTAR_PUB_DHARMAPUR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.MADHYA_BAHJANI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.HATI_NAMATI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.DAKSHIN_NALBARI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.UTTAR_PUB_KHATA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.PUB_NALBARI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.UPPER_BARBHAG_KHATA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.DIGHELI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.NATUN_DEHAR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.PANIGAON), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.GHOGRAPAR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.SILPOTABORI_LATIMA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.BARAJOL), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.NALBARI_DA, BuildingPermitAuthorityEnum.DIHJARI), "BPA_DA_GP");

		// Barpeta GP
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.BARPETA_DA, BuildingPermitAuthorityEnum.PATBAUSHI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.BARPETA_DA, BuildingPermitAuthorityEnum.SUNDARIDIA), "BPA_DA_GP");

		// Dibrugarh GP
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.RAJABHETA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.NIZ_MANKOTTA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.HILOIDHARI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.MANKOTTA_KHANIKAR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.ROMAI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.TIMONA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.MOHANBARI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.BOKUL), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.MAIJAN), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.NIZ_KANAI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.MODERKHAT), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.LAHOWAL), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.KOTOHA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.BORPOTHAR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.JOKAI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.LEJAI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.BARBARUA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.DULIA_KAKOTI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.KALAKHOWA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.GARUDHORIA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.DIBRUGARH_DA, BuildingPermitAuthorityEnum.BOGIBEEL), "BPA_DA_GP");

		// Tinsukia GP
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.RONGPURIA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.BORGURI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.BOJALTOLI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.ITAKHULI_CHARIALI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.LAIPULI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.DIMARUGURI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.BAPUJI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.GOTTONG), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.PANITOLA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.BAREKURI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.HAPJAN), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.JERAI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.TINSUKIA_DA, BuildingPermitAuthorityEnum.TENGAPANI), "BPA_DA_GP");

		// Silchar GP
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.MADHUR_BOND), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.BERENGA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.BHAGADHAR_BARJURAI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.AMBIKAPUR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.GHUNGOOR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.BHANJANTIPUR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.MEHERPUR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.KANAKPUR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.RONGPUR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.UTTAR_KRISHNAPUR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.DAKSHIN_KRISHNAPUR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.TUPKHANA), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.GHOUNGOOR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.BHORAKHAI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.TARUTAJABARI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.CHENKOORI), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.RAJNAGAR), "BPA_DA_GP");
		BUSINESS_SERVICE_MAP.put(new AuthorityKey(PlanningPermitAuthorityEnum.SILCHAR_DA, BuildingPermitAuthorityEnum.KUMARPARA_NIZJOINAGAR), "BPA_DA_GP");
	}

	@Autowired
	public WorkflowService(BPAConfiguration config, ServiceRequestRepository serviceRequestRepository,
			ObjectMapper mapper) {
		this.config = config;
		this.serviceRequestRepository = serviceRequestRepository;
		this.mapper = mapper;
	}

	/**
	 * Get the workflow config for the given tenant
	 * 
	 * @param tenantId
	 *            The tenantId for which businessService is requested
	 * @param requestInfo
	 *            The RequestInfo object of the request
	 * @return BusinessService for the the given tenantId
	 */
	public BusinessService getBusinessService(BPA bpa, RequestInfo requestInfo, String applicationNo) {
		StringBuilder url = getSearchURLWithParams(bpa, true, null);
		RequestInfoWrapper requestInfoWrapper = RequestInfoWrapper.builder().requestInfo(requestInfo).build();
		Object result = serviceRequestRepository.fetchResult(url, requestInfoWrapper);
		BusinessServiceResponse response = null;
		try {
			response = mapper.convertValue(result, BusinessServiceResponse.class);
		} catch (IllegalArgumentException e) {
			throw new CustomException(BPAErrorConstants.PARSING_ERROR, "Failed to parse response of calculate");
		}
		return response.getBusinessServices().get(0);
	}

	/**
	 * Creates url for search based on given tenantId
	 *
	 * @param tenantId
	 *            The tenantId for which url is generated
	 * @return The search url
	 */
	private StringBuilder getSearchURLWithParams(BPA bpa, boolean businessService, String applicationNo) {
		StringBuilder url = new StringBuilder(config.getWfHost());
		if (businessService) {
			url.append(config.getWfBusinessServiceSearchPath());
		} else {
			url.append(config.getWfProcessPath());
		}
		url.append("?tenantId=");
		url.append(bpa.getTenantId());
		if (businessService) {
				url.append("&businessServices=");
				url.append(bpa.getBusinessService());
		} else {
			url.append("&businessIds=");
			url.append(applicationNo);
		}
		return url;
	}

	/**
	 * Returns boolean value to specifying if the state is updatable
	 * 
	 * @param statusEnum
	 *            The stateCode of the bpa
	 * @param businessService
	 *            The BusinessService of the application flow
	 * @return State object to be fetched
	 */
	public Boolean isStateUpdatable(String status, BusinessService businessService) {
		for (org.egov.bpa.web.model.workflow.State state : businessService.getStates()) {
			if (state.getApplicationStatus() != null
					&& state.getApplicationStatus().equalsIgnoreCase(status.toString()))
				return state.getIsStateUpdatable();
		}
		return Boolean.FALSE;
	}

	/**
	 * Returns State name fo the current state of the document
	 * 
	 * @param statusEnum
	 *            The stateCode of the bpa
	 * @param businessService
	 *            The BusinessService of the application flow
	 * @return State String to be fetched
	 */
	public String getCurrentState(String status, BusinessService businessService) {
		for (State state : businessService.getStates()) {
			if (state.getApplicationStatus() != null
					&& state.getApplicationStatus().equalsIgnoreCase(status.toString()))
				return state.getState();
		}
		return null;
	}

	/**
	 * Returns State Obj fo the current state of the document
	 * 
	 * @param statusEnum
	 *            The stateCode of the bpa
	 * @param businessService
	 *            The BusinessService of the application flow
	 * @return State object to be fetched
	 */
	public State getCurrentStateObj(String status, BusinessService businessService) {
		for (State state : businessService.getStates()) {
			if (state.getApplicationStatus() != null
					&& state.getApplicationStatus().equalsIgnoreCase(status.toString()))
				return state;
		}
		return null;
	}


	/**
	 * Determining the business service based on the planning and building permit authorities.
	 * This method uses a predefined mapping to find the correct business service.
	 *
	 * @param areaMappingDetail The AreaMappingDetail containing the permit authorities.
	 * @return The determined business service or null if no valid combination is found.
	 */
	public String determineBusinessService(AreaMappingDetail areaMappingDetail) {
		PlanningPermitAuthorityEnum planning = areaMappingDetail.getPlanningPermitAuthority();
		BuildingPermitAuthorityEnum building = areaMappingDetail.getBuildingPermitAuthority();

		// Check if a workflow is configured in BUSINESS_SERVICE_MAP
		String businessService = BUSINESS_SERVICE_MAP.get(new AuthorityKey(planning, building));
		if (businessService == null) {
			log.info("Workflow not configured for the PlanningAuthority: {} and BuildingAuthority: {}", planning, building);
			throw new CustomException(BPAErrorConstants.WORKFLOW_NOT_CONFIGURED,
					"Workflow not configured for the PlanningAuthority: " + planning +
							" and BuildingAuthority: " + building);
		}

		log.debug("Evaluating business service with PlanningAuthority: {} and BuildingAuthority: {}", planning, building);

		String result = BUSINESS_SERVICE_MAP.get(new AuthorityKey(planning, building));

		if (result != null) {
			log.info("Matched business service: {}", result);
			return result;
		} else {
			log.warn("No valid combination found for PlanningAuthority: {} and BuildingAuthority: {}", planning, building);
			throw new CustomException(BPAErrorConstants.WORKFLOW_NOT_CONFIGURED,
					"Workflow not configured for the PlanningAuthority: " + planning +
							" and BuildingAuthority: " + building);
		}
	}
}
