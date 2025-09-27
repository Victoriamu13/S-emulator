package logic.engineFacade.api;
import logic.domain.execution.executer.ProgramExecuterImpl;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.program.SProgramImpl;
import logic.domain.program.info.ExpandedProgramInfo;
import logic.domain.program.info.ProgramInfoUtils;
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
    private SProgram currentProgram;
    private final LoadService loader = new LoadService();
    private String loadedXmlPath;

    //---Load program---
    @Override
    public LoadOutcome loadProgram(Path xmlPath) {
        LoadResult res = loader.loadFromXml(xmlPath, new CurrentAppState());
        if (res.success() && res.program() != null) {
            this.program = res.program();
            this.currentProgram = program;
            this.loadedXmlPath=xmlPath.toString();
            return LoadOutcome.ok();
        }
       return LoadOutcome.fail(res.errors());
    }

    private SProgram activeProgram() {
        return (currentProgram != null) ? currentProgram : program;
    }

    // --- Select program or function ---
    @Override
    public void selectProgramOrFunction(String name) {
        if (program == null) return;

        if (program.getName().equals(name)) {
            currentProgram = program;
        } else {
            String internalName = program.getFunctionLookup().internalNameOf(name);
            var body = program.getFunctionLookup().bodyOf(internalName);
            if (body != null && !body.isEmpty()) {
                currentProgram = new SProgramImpl(internalName);
                currentProgram.setFunctionLookup(program.getFunctionLookup());
                body.forEach(currentProgram::addInstruction);
            }
        }
    }

    @Override
    public boolean hasProgram() {
        return activeProgram() != null;
    }

    @Override
    public String getLoadedXmlPath() {  return loadedXmlPath;}

    //---Program info---
    @Override
    public String getProgramName() {
        return activeProgram() != null ? activeProgram().getName() : "";
    }

    //---instructions---
    @Override
    public List<InstructionDTO>getInstructionRows(int degree){
        int maxDegree=getMaxExpansionDegree();
        ProgramInfo info=getProgramInfo(activeProgram(),maxDegree,degree);

        return info.getInstructions().stream()
                .map(ins -> toDto(activeProgram(), ins))
                .toList();
    }


    @Override
    public int getInstructionBasicCount(int degree) {
        int maxDegree=getMaxExpansionDegree();
        var info = getProgramInfo(activeProgram(),maxDegree,degree);
        return (int) info.getInstructions().stream().filter(i -> !i.isSynthetic()).count();
    }

    @Override
    public int getInstructionSyntheticCount(int degree) {
        int maxDegree=getMaxExpansionDegree();
        var info = getProgramInfo(activeProgram(),maxDegree,degree);
        return (int) info.getInstructions().stream().filter(i -> i.isSynthetic()).count();
    }

    @Override
    public int getInstructionTotal(int degree) {
        int maxDegree=getMaxExpansionDegree();
        var info = getProgramInfo(activeProgram(),maxDegree,degree);
        return info.getInstructions().size();
    }


    @Override
    public List<String> getLabelsUsed(int degree) {
        int maxDegree=getMaxExpansionDegree();
        int used=validDegree(degree,maxDegree);
        return getProgramInfo(activeProgram(),getMaxExpansionDegree(),used).getLabelsUsed();
    }


    @Override
    public List<String> getAllVariablesUsed(int degree, Integer finalIndex) {
        Set<String> vars = new LinkedHashSet<>();
        int maxDegree=getMaxExpansionDegree();
        vars.addAll(getProgramInfo(activeProgram(),maxDegree,degree).getVariablesUsed());
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
        labels.addAll(getProgramInfo(activeProgram(),maxDegree,degree).getLabelsUsed());

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
        ProgramInfo info = getProgramInfo(activeProgram(),maxDegree,degree);
        List<InstructionInfo> chain;

        if (info instanceof ExpandedProgramInfo exp) {
            chain = exp.getExpansionForFinalIndex(used, finalIndex);
        } else {
            chain = info.getInstructions().stream()
                    .filter(ii -> ii.getIndex() == finalIndex)
                    .toList();
        }

        return chain.stream()
                .map(ii -> toDto(activeProgram(), ii))
                .sorted(Comparator.comparing(InstructionDTO::index).reversed())
                .toList();
    }


    @Override
    public int getMaxExpansionDegree() {
        ExpansionContext ctx = ExpansionContext.seedFrom(activeProgram());
        ProgramExpander  exp = new ProgramExpander(ctx);
        DegreeCalculator calc = new DegreeCalculator(exp);
        return calc.maxProgramDegree(activeProgram());
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
        return getProgramInfo(activeProgram(),maxDegree,used).getInputsUsed();
    }

    @Override
    public ExecutionReport runWithReport(int degree,long... inputs) {
        int maxDegree=getMaxExpansionDegree();
        int used = validDegree(degree,maxDegree);
        SProgram materialized = materializeProgram(activeProgram(),used);
        return new ProgramExecuterImpl(materialized).runWithReport(inputs);
    }

    //---Functions---
    @Override
    public List<String> getFunctionNames() {
        if (program == null) return List.of();
        return new ArrayList<>(program.getFunctionLookup().allFunctionNames());
    }

    @Override
    public List<InstructionDTO> getFunctionInstructionRows(String functionName) {
        if (activeProgram() == null) return List.of();
        var body = activeProgram().getFunctionLookup().bodyOf(functionName);
        if (body == null) return List.of();

        return body.stream()
                .map(ins -> ProgramInfoUtils.toInfo(ins, -1, null)) // SInstruction → InstructionInfo
                .map(ii -> toDto(activeProgram(), ii))
                .toList();
    }
}
