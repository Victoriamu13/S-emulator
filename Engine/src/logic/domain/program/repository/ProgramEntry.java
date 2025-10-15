package logic.domain.program.repository;

import logic.engineFacade.api.EngineFacade;

public record ProgramEntry(String progName, String uploader, int instCount, int maxDegree,
                           int runCount, double avgCreditCost) {
}
