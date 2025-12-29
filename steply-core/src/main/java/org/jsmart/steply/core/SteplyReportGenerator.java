package org.jsmart. steply.core;

import java.io.File;
import java.util.Map;

/**
 * SteplyReportGenerator: Simple wrapper.
 *
 * NOTE:  Zerocode-tdd automatically generates HTML and CSV reports to the configured
 * "zerocode.reports.folder" directory. This class can be used for additional custom
 * reporting if needed in the future.
 */
public class SteplyReportGenerator {

    /**
     * Zerocode already generates HTML reports.
     * This method is a no-op for now; custom reporting can be added here if needed.
     */
    public void generateHTMLReport(Map<String, Object> results, File outputDir) {
        // Zerocode handles HTML report generation automatically
        // See: zerocode.reports.folder system property
    }

    /**
     * Zerocode already generates CSV reports.
     * This method is a no-op for now; custom reporting can be added here if needed.
     */
    public void generateCSVReport(Map<String, Object> results, File outputDir) {
        // Zerocode handles CSV report generation automatically
        // See: zerocode.reports.folder system property
    }
}