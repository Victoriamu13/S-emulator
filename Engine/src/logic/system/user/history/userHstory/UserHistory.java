package logic.system.user.history.userHstory;

public record UserHistory(int runID, String progType, String name, String architecture,
                          int runDegree, double yValue, long totalCycles) {
}
