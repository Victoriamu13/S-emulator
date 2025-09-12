package logic.infrastructure.io.app;

import logic.domain.program.SProgram;

public class CurrentAppState {
    private SProgram currentProgram;

    public SProgram getCurrentProgram() {return currentProgram;}
    public void setProgram(SProgram currentProgram) {this.currentProgram = currentProgram;}
    public boolean hasProgram() {return currentProgram != null;}
}
