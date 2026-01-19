package org.orbitalLogistic.mission.application.ports.out;


import org.orbitalLogistic.mission.application.ports.in.CreateMissionCommand;

public interface ReportSender {

    void send(CreateMissionCommand rental);
}
