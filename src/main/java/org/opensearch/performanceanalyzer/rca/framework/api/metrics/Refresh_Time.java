/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.performanceanalyzer.rca.framework.api.metrics;


import org.opensearch.performanceanalyzer.commons.metrics.AllMetrics;
import org.opensearch.performanceanalyzer.rca.framework.api.Metric;

public class Refresh_Time extends Metric {
    public Refresh_Time(long evaluationIntervalSeconds) {
        super(AllMetrics.ShardStatsValue.REFRESH_TIME.name(), evaluationIntervalSeconds);
    }
}
