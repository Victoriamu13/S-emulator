package logic.system.programs.functions.repository;

public class FunctionEntry {
    private final String funcName;
    private final String userString;
    private final String progName;
    private final String uploader;
    private final int instCount;
    private final int maxDegree;

    public FunctionEntry(String funcName,String userString, String progName, String uploader, int instCount, int maxDegree) {
        this.funcName = funcName;
        this.userString= userString;
        this.progName = progName;
        this.uploader = uploader;
        this.instCount = instCount;
        this.maxDegree = maxDegree;
    }

    public String getFuncName(){ return funcName;}
    public String getUserString(){ return userString; }
    public String getProgName(){ return progName; }
    public String getUploader(){ return uploader; }
    public int getInstCount(){ return instCount; }
    public int getMaxDegree(){ return maxDegree; }
}
