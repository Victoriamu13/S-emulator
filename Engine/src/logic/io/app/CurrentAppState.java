package logic.io.app;

import logic.program.SProgram;

public class CurrentAppState {
    private SProgram currentProgram;

    public SProgram getCurrentProgram() {return currentProgram;}
    public void setProgram(SProgram currentProgram) {this.currentProgram = currentProgram;}
    public boolean hasProgram() {return currentProgram != null;}
}
