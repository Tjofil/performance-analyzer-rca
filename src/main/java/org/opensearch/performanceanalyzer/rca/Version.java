/*
 * Copyright OpenSearch Contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package org.opensearch.performanceanalyzer.rca;

/**
 * The version of the RCA framework and the graphs combined. The version is a combination of the
 * major and the minor versions together.
 */
public final class Version {
    // This will hopefully never change.
    public static final String RCA_VERSION_STR = "rca-version";

    /**
     * An increase in the major version means that the flow units are in-compatible and if different
     * instances(physical nodes) are running different versions of the framework, then the
     * transferred packets should be dropped. Every increment here should be accompanied with a line
     * describing the version bump.
     *
     * Note: The RCA version is agnostic of OpenSearch version.
     */
    static final class Major {
        // Bumping this post the Commons Lib(https://github.com/opensearch-project/performance-analyzer-commons/issues/2)
        // and Service Metrics(https://github.com/opensearch-project/performance-analyzer-commons/issues/8) change
        static final int RCA_MAJ_VERSION = 1;
    }

    /**
     * This is expected to increment with each noticeable change in the framework and also with
     * addition of new RCAs or enhancement of the old ones. But given this, we don't expect the
     * minor version to change for every single release and each increment should have a line
     * stating what changed.
     */
    static final class Minor {
        static final String RCA_MINOR_VERSION = ".0.1";
    }

    /** @return The version string. */
    public static String getRcaVersion() {
        return Major.RCA_MAJ_VERSION + Minor.RCA_MINOR_VERSION;
    }
}
