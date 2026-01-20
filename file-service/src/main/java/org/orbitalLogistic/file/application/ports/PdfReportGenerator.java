package org.orbitalLogistic.file.application.ports;

import org.orbitalLogistic.file.adapters.kafka.dto.CargoReportDataDTO;
import org.orbitalLogistic.file.adapters.kafka.dto.MissionReportDataDTO;
import org.orbitalLogistic.file.adapters.kafka.dto.UserReportDataDTO;

public interface PdfReportGenerator {
    byte[] generate(MissionReportDataDTO report);
    byte[] generate(UserReportDataDTO report);
    byte[] generate(CargoReportDataDTO report);
}
