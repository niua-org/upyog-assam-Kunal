package org.egov.bpa.web.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PlanningPermitAuthorityEnum {
    // Generic Types
    DA("DEVELOPMENT_AUTHORITY"),
    TACP("TOWN_AND_COUNTRY_PLANNING"),
    GMDA("GUWAHATI_METROPOLITAN_DEVELOPMENT_AUTHORITY"),

    // District-Specific Development Authorities
    NALBARI_DA("NALBARI_DEVELOPMENT_AUTHORITY"),
    BARPETA_DA("BARPETA_DEVELOPMENT_AUTHORITY"),
    DIBRUGARH_DA("DIBRUGARH_DEVELOPMENT_AUTHORITY"),
    TINSUKIA_DA("TINSUKIA_DEVELOPMENT_AUTHORITY"),
    SILCHAR_DA("SILCHAR_DEVELOPMENT_AUTHORITY");

    private final String value;
}
