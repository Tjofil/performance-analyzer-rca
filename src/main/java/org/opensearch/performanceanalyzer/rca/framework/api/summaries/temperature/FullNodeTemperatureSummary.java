/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.performanceanalyzer.rca.framework.api.summaries.temperature;


import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.protobuf.GeneratedMessageV3;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jooq.Field;
import org.jooq.impl.DSL;
import org.opensearch.performanceanalyzer.grpc.FlowUnitMessage;
import org.opensearch.performanceanalyzer.rca.framework.api.summaries.HotNodeSummary;
import org.opensearch.performanceanalyzer.rca.framework.core.GenericSummary;
import org.opensearch.performanceanalyzer.rca.framework.core.temperature.TemperatureDimension;
import org.opensearch.performanceanalyzer.rca.framework.core.temperature.TemperatureVector;

/**
 * Full Node Temperature Summary contains the Node Details (ID and Address) and the list of
 * dimensional summaries for a node. This summary is used to construct the compact node level
 * summary which is passed over the wire to the cluster_manager which is then used to construct the
 * cluster temperature profile.
 */
public class FullNodeTemperatureSummary extends GenericSummary {
    private static final Logger LOG = LogManager.getLogger(FullNodeTemperatureSummary.class);

    public static final String TABLE_NAME = FullNodeTemperatureSummary.class.getSimpleName();

    /**
     * A node has a temperature profile of its own. The temperature profile of a node is the mean
     * temperature along each dimension.
     */
    private final TemperatureVector temperatureVector;

    /**
     * A node also has the complete list of shards in each dimension, broken down by the different
     * temperature zones.
     */
    private final NodeLevelDimensionalSummary[] nodeDimensionProfiles;

    private final String nodeId;
    private final String hostAddress;

    public FullNodeTemperatureSummary(String nodeId, String hostAddress) {
        this.nodeId = nodeId;
        this.hostAddress = hostAddress;
        this.nodeDimensionProfiles =
                new NodeLevelDimensionalSummary[TemperatureDimension.values().length];
        this.temperatureVector = new TemperatureVector();
    }

    public TemperatureVector getTemperatureVector() {
        return temperatureVector;
    }

    public List<NodeLevelDimensionalSummary> getNodeDimensionProfiles() {
        return Arrays.asList(nodeDimensionProfiles);
    }

    public String getNodeId() {
        return nodeId;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public void updateNodeDimensionProfile(NodeLevelDimensionalSummary nodeDimensionProfile) {
        TemperatureDimension dimension = nodeDimensionProfile.getProfileForDimension();
        this.nodeDimensionProfiles[dimension.ordinal()] = nodeDimensionProfile;
        this.temperatureVector.updateTemperatureForDimension(
                dimension, nodeDimensionProfile.getMeanTemperature());
    }

    public List<GenericSummary> getNestedSummaryList() {
        List<GenericSummary> dimensionalSummaries = new ArrayList<>();
        for (NodeLevelDimensionalSummary dimSummary : nodeDimensionProfiles) {
            if (dimSummary != null) {
                dimensionalSummaries.add(dimSummary);
            }
        }
        return dimensionalSummaries;
    }

    @Override
    public <T extends GeneratedMessageV3> T buildSummaryMessage() {
        throw new IllegalStateException(
                "FullNodeTemperatureSummary should not be transported " + "over the wire.");
    }

    @Override
    public void buildSummaryMessageAndAddToFlowUnit(FlowUnitMessage.Builder messageBuilder) {
        throw new IllegalStateException(
                "FullNodeTemperatureSummary should not be received over " + "the wire.");
    }

    @Override
    public String getTableName() {
        return TABLE_NAME;
    }

    @Override
    public List<Field<?>> getSqlSchema() {
        List<Field<?>> schema = new ArrayList<>();

        schema.add(
                DSL.field(
                        DSL.name(HotNodeSummary.SQL_SCHEMA_CONSTANTS.NODE_ID_COL_NAME),
                        String.class));
        schema.add(
                DSL.field(
                        DSL.name(HotNodeSummary.SQL_SCHEMA_CONSTANTS.HOST_IP_ADDRESS_COL_NAME),
                        String.class));

        for (TemperatureDimension dimension : TemperatureDimension.values()) {
            schema.add(DSL.field(DSL.name(dimension.NAME), Short.class));
        }
        return schema;
    }

    @Override
    public List<Object> getSqlValue() {
        List<Object> values = new ArrayList<>();

        values.add(getNodeId());
        values.add(getHostAddress());

        for (TemperatureDimension dimension : TemperatureDimension.values()) {
            values.add(temperatureVector.getTemperatureFor(dimension));
        }
        return values;
    }

    @Override
    public JsonElement toJson() {
        JsonObject summaryObj = new JsonObject();
        for (TemperatureDimension dimension : TemperatureDimension.values()) {
            TemperatureVector.NormalizedValue value =
                    temperatureVector.getTemperatureFor(dimension);
            summaryObj.addProperty(dimension.NAME, value != null ? value.getPOINTS() : null);
        }
        getNestedSummaryList()
                .forEach(
                        summary -> {
                            summaryObj.add(summary.getTableName(), summary.toJson());
                        });
        return summaryObj;
    }
}
