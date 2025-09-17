package logic.engineFacade.api;
import logic.domain.execution.executer.ProgramExecuterImpl;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.program.info.ExpandedProgramInfo;
import logic.domain.variable.SVarsType;
import logic.engineFacade.model.LoadOutcome;
import logic.engineFacade.model.ExecutionReport;
import logic.domain.expand.expandProgram.DegreeCalculator;
import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.expandProgram.ProgramExpander;
import logic.engineFacade.model.InstructionDTO;
import logic.infrastructure.io.app.CurrentAppState;
import logic.infrastructure.io.xml.load.LoadResult;
import logic.infrastructure.io.xml.load.LoadService;
import logic.domain.program.SProgram;
import logic.domain.program.info.ProgramInfo;
import java.nio.file.Path;
import java.util.*;

import static logic.engineFacade.api.EngineFacadeUtils.*;

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
    public String getProgramName() {
        return program != null ? program.getName() : "";
    }

    //---instructions---
    @Override
    public List<InstructionDTO>getInstructionRows(int degree){
        int maxDegree=getMaxExpansionDegree();
        int used=validDegree(degree,maxDegree);
        ProgramInfo info=getProgramInfo(program,maxDegree,degree);
        return info.getInstructions().stream()
                .map(ins->new InstructionDTO(
                        ins.getIndex(),ins.isSynthetic() ? "S" : "B", ins.getVariableName(),
                        ins.getLabelName(),ins.getFullCommand(), ins.getCycles())).toList();
    }


    @Override
    public int getInstructionBasicCount(int degree) {
        int maxDegree=getMaxExpansionDegree();
        var info = getProgramInfo(program,maxDegree,degree);
        return (int) info.getInstructions().stream().filter(i -> !i.isSynthetic()).count();
    }

    @Override
    public int getInstructionSyntheticCount(int degree) {
        int maxDegree=getMaxExpansionDegree();
        var info = getProgramInfo(program,maxDegree,degree);
        return (int) info.getInstructions().stream().filter(i -> i.isSynthetic()).count();
    }

    @Override
    public int getInstructionTotal(int degree) {
        int maxDegree=getMaxExpansionDegree();
        var info = getProgramInfo(program,maxDegree,degree);
        return info.getInstructions().size();
    }


    @Override
    public List<String> getLabelsUsed(int degree) {
        int maxDegree=getMaxExpansionDegree();
        int used=validDegree(degree,maxDegree);
        return getProgramInfo(program,getMaxExpansionDegree(),used).getLabelsUsed();
    }


    @Override
    public List<String> getAllVariablesUsed(int degree, Integer finalIndex) {
        Set<String> vars = new LinkedHashSet<>();
        int maxDegree=getMaxExpansionDegree();
        vars.addAll(getProgramInfo(program,maxDegree,degree).getVariablesUsed());
        vars.add(SVarsType.RESULT.getVarRepresentation(0));

        if (finalIndex != null) {
            getExpansionHistoryChain(degree, finalIndex).forEach(ii -> {
                if (ii.variable() != null && !ii.variable().isBlank() && !ii.command().toUpperCase().startsWith("GOTO")) {
                    vars.add(ii.variable());
                }
            });
        }

        return vars.stream().sorted(numericAwareComparator()).toList();
    }

    @Override
    public List<String> getAllLabelsUsed(int degree,Integer finalIndex) {
        Set<String> labels = new LinkedHashSet<>();
        int maxDegree=getMaxExpansionDegree();
        labels.addAll(getProgramInfo(program,maxDegree,degree).getLabelsUsed());

        if (finalIndex != null) {
            getExpansionHistoryChain(degree, finalIndex).forEach(ii -> {
                if (ii.label() != null && !ii.label().isBlank()) {
                    labels.add(ii.label());
                }
            });
        }
        return labels.stream().sorted(numericAwareComparator()).toList();
    }

    //Expansion---
    @Override
    public List<InstructionDTO>getExpansionHistoryChain(int degree, int finalIndex){
        int maxDegree=getMaxExpansionDegree();
        int used = validDegree(degree,maxDegree);
        ProgramInfo info = getProgramInfo(program,maxDegree,degree);
        List<InstructionInfo> chain;

        if (info instanceof ExpandedProgramInfo exp) {
            chain = exp.getExpansionForFinalIndex(used, finalIndex);
        } else {
            chain = info.getInstructions().stream()
                    .filter(ii -> ii.getIndex() == finalIndex)
                    .toList();
        }

        return chain.stream()
                .map(ii -> new InstructionDTO(
                        ii.getIndex(), ii.isSynthetic() ? "S" : "B", ii.getVariableName(),
                        ii.getLabelName(), ii.getFullCommand(), ii.getCycles()))
                .sorted(Comparator.comparing(InstructionDTO::index).reversed()).toList();
    }


    @Override
    public int getMaxExpansionDegree() {
        ExpansionContext ctx = ExpansionContext.seedFrom(program);
        ProgramExpander  exp = new ProgramExpander(ctx);
        DegreeCalculator calc = new DegreeCalculator(exp);
        return calc.maxProgramDegree(program);
    }

    //---Execute program---
    @Override
    public long[] parseInputsCsv(String csv, int degree) {
        List<String> inputsUsed = getInputsUsed(degree);
        int required = getRequiredInputsCount(inputsUsed);
        if (csv == null || csv.isBlank()) {
            return new long[required];
        }
        List<String> values = Arrays.stream(csv.split(",")).toList();
        return parseInputValues(values, required,null);
    }

    @Override
    public long[] prepareInputsFields(int degree, List<String> rawValues) {
        List<String> inputsUsed = getInputsUsed(degree);
        int required = getRequiredInputsCount(inputsUsed);
        return parseInputValues(rawValues, required,inputsUsed);
    }


    @Override
    public List<String> getInputsUsed(int degree) {
        int maxDegree=getMaxExpansionDegree();
        int used=validDegree(degree,maxDegree);
        return getProgramInfo(program,maxDegree,used).getInputsUsed();
    }

    @Override
    public ExecutionReport runWithReport(int degree,long... inputs) {
        int maxDegree=getMaxExpansionDegree();
        int used = validDegree(degree,maxDegree);
        SProgram materialized = materializeProgram(program,used);
        return new ProgramExecuterImpl(materialized).runWithReport(inputs);
    }

}
