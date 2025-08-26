package logic.engineFacade;

import logic.execution.executer.ProgramExecuter;
import logic.execution.executer.ProgramExecuterImpl;
import logic.expand.expandProgram.DegreeCalculator;
import logic.expand.expandProgram.ExpansionContext;
import logic.expand.expandProgram.ProgramExpander;
import logic.program.info.ExpandedProgramInfo;
import logic.program.info.ProgramInfo;
import logic.program.info.ProgramInfoImpl;
import logic.program.SProgram;


public class EngineFacadeImpl implements EngineFacade {

    //program + execute
    private final ProgramExecuter currExecuter;
    private final SProgram program;

    public EngineFacadeImpl(SProgram program) {
        this.program = program;
        this.currExecuter = new ProgramExecuterImpl(program);
    }

    @Override public long runFullProgram(long... inputs) {return currExecuter.run(inputs);}

    @Override public int getMaxExpansionDegree() {
        ExpansionContext tempCtx = ExpansionContext.seedFrom(program);
        ProgramExpander  tempExp = new ProgramExpander(tempCtx);
        DegreeCalculator calc  = new DegreeCalculator(tempExp);
        return calc.maxProgramDegree(program);
    }

    @Override
    public ProgramInfo getProgramInfo(int degree) {
        if (degree == 0) {return new ProgramInfoImpl(program);} ////////need to check valid degree///////////

        ExpansionContext ctx = ExpansionContext.seedFrom(program);
        ProgramExpander  exp = new ProgramExpander(ctx);
        return new ExpandedProgramInfo(program, degree, exp, ctx);
    }

}
