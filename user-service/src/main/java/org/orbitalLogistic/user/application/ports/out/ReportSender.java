package org.orbitalLogistic.user.application.ports.out;

import org.orbitalLogistic.user.application.ports.in.RegisterCommand;

public interface ReportSender {
    void send(RegisterCommand user);
}
