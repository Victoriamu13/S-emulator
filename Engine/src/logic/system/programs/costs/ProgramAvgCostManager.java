package logic.system.programs.costs;

import java.util.concurrent.ConcurrentHashMap;

public class ProgramAvgCostManager {
    // Map: programName → averageCost
    private static final ConcurrentHashMap<String, Double> avgCosts = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Integer> runCounts = new ConcurrentHashMap<>();

 //update average program cost after successful run
    public static synchronized void updateAverageCost(String programName, long progCost) {
        double prevAvg = avgCosts.getOrDefault(programName, 0.0);
        int prevCount = runCounts.getOrDefault(programName, 0);

        double newAvg;
        if (prevCount == 0) {
            newAvg = progCost; // At the beginning : avgCost = first run
        } else {
            newAvg = ((prevAvg * prevCount) + progCost) / (prevCount + 1);
        }

        avgCosts.put(programName, newAvg);
        runCounts.put(programName, prevCount + 1);
    }

    // Get program average cost
    public static synchronized double getAverageCost(String programName) {
        return avgCosts.getOrDefault(programName, 0.0);
    }

    // Get number of runs based on program
    public static synchronized int getRunCount(String programName) {
        return runCounts.getOrDefault(programName, 0);
    }
}
