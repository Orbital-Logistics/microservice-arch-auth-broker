package org.orbitalLogistic.cargo.application.ports.out;

import org.orbitalLogistic.cargo.domain.model.CargoStorage;

public interface ReportSender {
    void send(CargoStorage storage);
}

