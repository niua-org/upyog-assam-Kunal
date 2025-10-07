package org.egov.edcr.feature;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.Block;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.utility.DcrConstants;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AdditionalFeatureExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(AdditionalFeatureExtract.class);

    @Override
    public PlanDetail extract(PlanDetail pl) {
        return pl;
    }

    @Override
    public PlanDetail validate(PlanDetail pl) {
        LOG.info("Starting of AdditionalFeatureExtract validate method");
        HashMap<String, String> errors = new HashMap<>();
        List<Block> blocks = pl.getBlocks();
        for (Block block : blocks)
            if (block.getBuilding() != null && block.getBuilding().getBuildingHeight().compareTo(BigDecimal.ZERO) == 0) {
                errors.put(String.format(DcrConstants.BLOCK_BUILDING_HEIGHT, block.getNumber()),
                        edcrMessageSource.getMessage(DcrConstants.OBJECTNOTDEFINED,
                                new String[] { String.format(DcrConstants.BLOCK_BUILDING_HEIGHT, block.getNumber()) },
                                LocaleContextHolder.getLocale()));
                pl.addErrors(errors);
            }
        LOG.info("Ending of AdditionalFeatureExtract validate method");
        return pl;
    }

}
