package org.egov.edcr.feature;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.egov.common.entity.edcr.*;
import org.egov.edcr.entity.blackbox.MeasurementDetail;
import org.egov.edcr.entity.blackbox.PlanDetail;
import org.egov.edcr.service.LayerNames;
import org.egov.edcr.utility.Util;
import org.kabeja.dxf.DXFLWPolyline;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.*;

public class ArchitecturalFeatureExtract extends FeatureExtract {
    private static final Logger LOG = LogManager.getLogger(ArchitecturalFeatureExtract.class);

    @Autowired
    private LayerNames layerNames;

    @Override
    public PlanDetail validate(PlanDetail planDetail) {
        return planDetail;
    }

    @Override
    public PlanDetail extract(PlanDetail pl) {
        LOG.info("Starting of ArchitecturalFeatureExtract extract method");
        Set<String> roomOccupancyTypes = new HashSet<>();
        Map<String, Integer> architecturalFeature = pl.getSubFeatureColorCodesMaster().get("architecturalFeature");
        roomOccupancyTypes.addAll(architecturalFeature.keySet());
        for (Block block : pl.getBlocks()) {
            if (block.getBuilding() != null && !block.getBuilding().getFloors().isEmpty())
                for (Floor floor : block.getBuilding().getFloors()) {

                    LOG.info("Processing ArchitecturalFeature for Block: " + block.getNumber() + " Floor: " + floor.getNumber());
                    Map<Integer, List<BigDecimal>> architecturalFeatureHeightMap = new HashMap<>();
                    String architecturalFeatureLayerName = String.format(layerNames.getLayerName("BLK_" + block.getNumber() + "FLR_" + floor.getNumber() + "_ARCHITECTURAL_FEATURE"), "+\\d");
                    List<String> architecturalFeatureLayers = Util.getLayerNamesLike(pl.getDoc(), architecturalFeatureLayerName);

                    if (!architecturalFeatureLayers.isEmpty()) {
                        for (String architecturalFeatureLayer : architecturalFeatureLayers) {
                            for (String type : roomOccupancyTypes) {
                                Integer colorCode = architecturalFeature.get(type);
                                List<BigDecimal> servicearchitectureHeights = Util.getListOfDimensionByColourCode(pl, architecturalFeatureLayer, colorCode);
                                if (!servicearchitectureHeights.isEmpty())
                                    architecturalFeatureHeightMap.put(colorCode, servicearchitectureHeights);
                            }

                            List<DXFLWPolyline> roomPolyLines = Util.getPolyLinesByLayer(pl.getDoc(), architecturalFeatureLayer);

                            if (!architecturalFeatureHeightMap.isEmpty() || !roomPolyLines.isEmpty()) {

                                boolean isClosed = roomPolyLines.stream().allMatch(dxflwPolyline -> dxflwPolyline.isClosed());

                                ArchitecturalFeature architecture = new ArchitecturalFeature();
                                String[] architectureNo = architecturalFeatureLayer.split("_");
                                if (architectureNo != null && architectureNo.length == 7) {
                                    architecture.setNumber(architectureNo[6]);
                                }
                                architecture.setClosed(isClosed);

                                List<RoomHeight> architectureHeights = new ArrayList<>();
                                if (!roomPolyLines.isEmpty()) {
                                    List<Measurement> architectures = new ArrayList<Measurement>();
                                    roomPolyLines.stream().forEach(rp -> {
                                        Measurement m = new MeasurementDetail(rp, true);
                                        if (!architecturalFeatureHeightMap.isEmpty() && architecturalFeatureHeightMap.containsKey(m.getColorCode())) {
                                            for (BigDecimal value : architecturalFeatureHeightMap.get(m.getColorCode())) {
                                                RoomHeight roomHeight = new RoomHeight();
                                                roomHeight.setColorCode(m.getColorCode());
                                                roomHeight.setHeight(value);
                                                architectureHeights.add(roomHeight);
                                            }
                                            architecture.setHeights(architectureHeights);
                                        }
                                        architectures.add(m);
                                    });
                                    architecture.setArchitectures(architectures);
                                }
                                floor.addArchitecturalFeature(architecture);
                            }
                        }
                    }
                }
        }
        LOG.info("Ending of ArchitecturalFeatureExtract extract method");
        return pl;
    }
}
