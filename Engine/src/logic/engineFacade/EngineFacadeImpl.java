package logic.engineFacade;

import logic.execution.ProgramExecuter;
import logic.execution.ProgramExecuterImpl;
import logic.program.SProgram;


public class EngineFacadeImpl implements EngineFacade {
    private final ProgramExecuter currExecuter;
    private final SProgram program;


    public EngineFacadeImpl(SProgram program) {
        this.program = program;
        this.currExecuter = new ProgramExecuterImpl(program);
    }

    @Override
    public long runFullProgram(long... inputs) {
        return currExecuter.run(inputs);
    }

}
