package logic.engineFacade.facade;

import logic.engineFacade.report.ExecutionReport;
import logic.execution.executer.ProgramExecuterImpl;
import logic.expand.expandProgram.DegreeCalculator;
import logic.expand.expandProgram.ExpansionContext;
import logic.expand.expandProgram.ProgramExpander;
import logic.instructions.SInstruction;
import logic.program.SProgram;
import logic.program.SProgramImpl;
import logic.program.info.ExpandedProgramInfo;
import logic.program.info.ProgramInfo;
import logic.program.info.ProgramInfoImpl;

import java.util.List;

public class EngineFacadeImpl implements EngineFacade {
    private final SProgram program;

    public EngineFacadeImpl(SProgram program) {
        this.program = program;
    }

    @Override
    public int getMaxExpansionDegree() {
        ExpansionContext ctx = ExpansionContext.seedFrom(program);
        ProgramExpander  exp = new ProgramExpander(ctx);
        DegreeCalculator calc = new DegreeCalculator(exp);
        return calc.maxProgramDegree(program);
    }

    @Override
    public ProgramInfo getProgramInfo(int degree) {
        int used = validDegree(degree);
        if (used == 0) {
            return new ProgramInfoImpl(program);
        }
        ExpansionContext ctx = ExpansionContext.seedFrom(program);
        ProgramExpander  exp = new ProgramExpander(ctx);
        return new ExpandedProgramInfo(program, used, exp, ctx);
    }

    @Override
    public ExecutionReport runWithReport(int degree, long... inputs) {
        int used = validDegree(degree);
        SProgram materialized = materializeProgram(used);
        return new ProgramExecuterImpl(materialized).runWithReport(inputs);
    }


    //-----helper func-----
    private int validDegree(int degree) {
        int max = getMaxExpansionDegree();
        if (degree < 0) return 0;
        if (degree > max) return max;
        return degree;
    }

    private SProgram materializeProgram(int usedDegree) {
        if (usedDegree == 0) return program; // regular program - no expansion

        // expand program to degree
        ExpansionContext ctx = ExpansionContext.seedFrom(program);
        ProgramExpander  exp = new ProgramExpander(ctx);
        List<SInstruction> expanded = exp.expandToDegree(program.getInstructions(), usedDegree);

        SProgramImpl expandedProg = new SProgramImpl(program.getName() + "_deg" + usedDegree);
        for (SInstruction ins : expanded) expandedProg.addInstruction(ins);
        return expandedProg;
    }
}
