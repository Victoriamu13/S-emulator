package logic.system.programs.repository;

public class ProgramEntry {
    private final String progName;
    private final String uploader;
    private final int instCount;
    private final int maxDegree;
    private final int runCount;
    private final double avgCreditCost;

    public ProgramEntry(String progName, String uploader, int instCount, int maxDegree,
                        int runCount, double avgCreditCost) {
        this.progName = progName;
        this.uploader = uploader;
        this.instCount = instCount;
        this.maxDegree = maxDegree;
        this.runCount = runCount;
        this.avgCreditCost = avgCreditCost;
    }

    // === GETTERS ===
    public String getProgName() { return progName; }
    public String uploadedBy() { return uploader; }
    public int getNumberInstructions() { return instCount; }
    public int getMaxExpansionDegree() { return maxDegree; }
    public int getRunCount() { return runCount; }
    public double getAvgCreditCost() { return avgCreditCost; }
}

