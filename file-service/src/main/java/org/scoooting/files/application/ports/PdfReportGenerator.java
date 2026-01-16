package org.scoooting.files.application.ports;

import org.scoooting.files.adapters.kafka.dto.ReportDataDTO;

public interface PdfReportGenerator {
    byte[] generate(ReportDataDTO report);
}
