package logic.engineFacade.api;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.program.info.ExpandedProgramInfo;
import logic.engineFacade.model.LoadOutcome;
import logic.engineFacade.model.ExecutionReport;
import logic.domain.execution.executer.ProgramExecuterImpl;
import logic.domain.expand.expandProgram.DegreeCalculator;
import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.expandProgram.ProgramExpander;
import logic.domain.instructions.SInstruction;
import logic.engineFacade.model.InstructionDTO;
import logic.infrastructure.io.app.CurrentAppState;
import logic.infrastructure.io.xml.load.LoadResult;
import logic.infrastructure.io.xml.load.LoadService;
import logic.domain.program.SProgram;
import logic.domain.program.SProgramImpl;
import logic.domain.program.info.ProgramInfo;
import logic.domain.program.info.ProgramInfoImpl;

import java.nio.file.Path;
import java.util.List;

public class EngineFacadeImpl implements EngineFacade {
    private SProgram program;
    private final LoadService loader = new LoadService();
    private String loadedXmlPath;

    //---Load program---
    @Override
    public LoadOutcome loadProgram(Path xmlPath) {
        LoadResult res = loader.loadFromXml(xmlPath, new CurrentAppState());
        if (res.success && res.program != null) {
            this.program = res.program;
            this.loadedXmlPath=xmlPath.toString();
            return LoadOutcome.ok();
        }
       return LoadOutcome.fail(res.errors);
    }

    @Override
    public boolean hasProgram() {
        return program != null;
    }

    @Override
    public String getLoadedXmlPath() {  return loadedXmlPath;}

    //---Program info---
    @Override
    public int getMaxExpansionDegree() {
        ExpansionContext ctx = ExpansionContext.seedFrom(program);
        ProgramExpander  exp = new ProgramExpander(ctx);
        DegreeCalculator calc = new DegreeCalculator(exp);
        return calc.maxProgramDegree(program);
    }


    @Override
    public List<String> getInputsUsed(int degree) {
        return getProgramInfo(validDegree(degree)).getInputsUsed();
    }

    @Override
    public List<String> getLabelsUsed(int degree) {
        return getProgramInfo(validDegree(degree)).getLabelsUsed();
    }

    @Override
    public String getProgramName() {
        return program != null ? program.getName() : "";
    }

    //---instructions---
    @Override
    public List<InstructionDTO>getInstructionRows(int degree){
        int used=validDegree(degree);
        ProgramInfo info=getProgramInfo(used);
        return info.getInstructions().stream()
                .map(ins->new InstructionDTO(
                        ins.getIndex(),ins.getOriginIndex(),ins.isSynthetic() ? "S" : "B",
                        ins.getLabelName(),ins.getFullCommand(), ins.getCycles())).toList();
    }

    @Override
    public List<InstructionDTO>getExpansionHistoryChain(int degree, int finalIndex){
        int used = validDegree(degree);
        ProgramInfo info = getProgramInfo(used);
        List<InstructionInfo> chain;

        if (info instanceof ExpandedProgramInfo exp) {
            // מעבירים גם את הדרגה וגם את האינדקס כדי לקבל את ההרחבה קדימה
            chain = exp.getExpansionForFinalIndex(used, finalIndex);
        } else {
            // fallback – אם זה ProgramInfo רגיל בלי הרחבות
            chain = info.getInstructions().stream()
                    .filter(ii -> ii.getIndex() == finalIndex)
                    .toList();
        }

        return chain.stream()
                .map(ii -> new InstructionDTO(
                        ii.getIndex(),
                        ii.getOriginIndex(),
                        ii.isSynthetic() ? "S" : "B",
                        ii.getLabelName(),
                        ii.getFullCommand(),
                        ii.getCycles()
                ))
                .toList();
    }


    @Override
    public int getInstructionBasicCount(int degree) {
        var info = getProgramInfo(degree);
        return (int) info.getInstructions().stream().filter(i -> !i.isSynthetic()).count();
    }

    @Override
    public int getInstructionSyntheticCount(int degree) {
        var info = getProgramInfo(degree);
        return (int) info.getInstructions().stream().filter(i -> i.isSynthetic()).count();
    }

    //---Execute program---
    @Override
    public ExecutionReport runWithReport(int degree, long... inputs) {
        int used = validDegree(degree);
        SProgram materialized = materializeProgram(used);
        return new ProgramExecuterImpl(materialized).runWithReport(inputs);
    }

    @Override
    public int getInstructionTotal(int degree) {
        var info = getProgramInfo(degree);
        return info.getInstructions().size();
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

    public ProgramInfo getProgramInfo(int degree) {
        int used = validDegree(degree);
        if (used == 0) {
            return new ProgramInfoImpl(program);
        }
        ExpansionContext ctx = ExpansionContext.seedFrom(program);
        ProgramExpander  exp = new ProgramExpander(ctx);
        return new ExpandedProgramInfo(program, used, exp, ctx);
    }

}
