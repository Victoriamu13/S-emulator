package logic.system.user.history.userHstory;

import logic.engineFacade.model.RunRecord;

public record UserHistory(int runID, String progType, String name, String architecture,
                          String executedBy, RunRecord runRecord) {

    public int runDegree() {
        return runRecord != null ? runRecord.degree() : 0;
    }

    public double yValue() {
        return runRecord != null ? runRecord.yValue() : 0.0;
    }

    public long totalCycles() {
        return runRecord != null ? runRecord.cycles() : 0L;
    }

    public String executedBy(){
        return executedBy != null ? executedBy : null;
    }
}
