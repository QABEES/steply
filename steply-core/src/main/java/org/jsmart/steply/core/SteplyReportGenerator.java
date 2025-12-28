package org.jsmart.steply.core;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

/**
 * Generates simple HTML and CSV reports as an MVP.
 * If zerocode report generator is available, integration can be added later.
 */
public class SteplyReportGenerator {

    public void generateHTMLReport(Map<String, Object> results, File outputDir) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File html = new File(outputDir, "index.html");
        try (FileWriter fw = new FileWriter(html)) {
            fw.write("<html><head><title>Steply Report</title></head><body>");
            fw.write("<h1>Steply Test Execution Report</h1>");
            fw.write("<table border=\"1\"><tr><th>Metric</th><th>Value</th></tr>");
            for (Map.Entry<String, Object> e : results.entrySet()) {
                fw.write("<tr><td>" + e.getKey() + "</td><td>" + e.getValue() + "</td></tr>");
            }
            fw.write("</table></body></html>");
        }
    }

    public void generateCSVReport(Map<String, Object> results, File outputDir) throws IOException {
        if (!outputDir.exists()) {
            outputDir.mkdirs();
        }
        File csv = new File(outputDir, "summary.csv");
        try (FileWriter fw = new FileWriter(csv)) {
            for (Map.Entry<String, Object> e : results.entrySet()) {
                fw.write(String.format("\"%s\",\"%s\"\n", e.getKey(), e.getValue()));
            }
        }
    }
}