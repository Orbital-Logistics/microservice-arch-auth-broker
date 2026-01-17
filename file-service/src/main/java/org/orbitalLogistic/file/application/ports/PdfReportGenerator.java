package org.orbitalLogistic.file.application.ports;

import org.orbitalLogistic.file.adapters.kafka.dto.ReportDataDTO;

public interface PdfReportGenerator {
    byte[] generate(ReportDataDTO report);
}
