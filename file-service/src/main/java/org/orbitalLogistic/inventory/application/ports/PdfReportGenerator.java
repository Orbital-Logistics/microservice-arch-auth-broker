package org.orbitalLogistic.inventory.application.ports;

import org.orbitalLogistic.inventory.adapters.kafka.dto.ReportDataDTO;

public interface PdfReportGenerator {
    byte[] generate(ReportDataDTO report);
}
