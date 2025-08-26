package logic.engineFacade;
import logic.program.info.ProgramInfo;

public interface EngineFacade {
    long runFullProgram(long... inputs);
    ProgramInfo getProgramInfo(int degree);
    int getMaxExpansionDegree();
}
