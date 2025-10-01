package logic.engineFacade.api;
import logic.domain.execution.context.CurrentContext;
import logic.domain.execution.context.CurrentContextImpl;
import logic.domain.execution.executer.ProgramExecuterImpl;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.program.SProgramImpl;
import logic.domain.program.info.ExpandedProgramInfo;
import logic.domain.program.info.ProgramInfoUtils;
import logic.domain.variable.SVarsType;
import logic.engineFacade.model.debug.DebugSession;
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
    private DebugSession activeDebug;
    private Map<Integer, List<String>> cachedInputs = new HashMap<>();
    private Integer cachedMaxDegree = null;

    //---Load program---
    @Override
    public LoadOutcome loadProgram(Path xmlPath) {
        LoadResult res = loader.loadFromXml(xmlPath, new CurrentAppState());
        if (res.success() && res.program() != null) {
            this.program = res.program();
            this.currentProgram = program;
            this.loadedXmlPath=xmlPath.toString();
             resetExpansionCache();
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

    @Override
    public void resetExpansionCache() {
        cachedInputs.clear();
        cachedMaxDegree = null;
    }

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
    if (cachedMaxDegree != null) {
        return cachedMaxDegree;
    }

    SProgram freshCopy = EngineFacadeUtils.materializeProgram(activeProgram(), 0);
    ExpansionContext ctx = ExpansionContext.seedFrom(activeProgram());
    ProgramExpander  exp = new ProgramExpander(ctx);
    DegreeCalculator calc = new DegreeCalculator(exp);

    return calc.maxProgramDegree(freshCopy);
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

    @Override
    public boolean startDebugSession(int degree, long... inputs) {
        int maxDegree=getMaxExpansionDegree();
        int used = validDegree(degree,maxDegree);
        SProgram prog = materializeProgram(activeProgram(),used);

        CurrentContext ctx = new CurrentContextImpl(inputs, prog.getFunctionLookup());
        List<SInstruction> instructions=prog.getInstructions();

        activeDebug=new DebugSession(instructions,ctx);
        return true;
    }

    @Override
    public ExecutionReport stepOver() {
        if (activeDebug == null) return null;
        return activeDebug.step();
    }

    @Override
    public ExecutionReport stepBack() {
        if (activeDebug == null) return null;
        return activeDebug.stepBack();
    }

    @Override
    public ExecutionReport resume() {
        if (activeDebug == null) return null;

        DebugSession dbg = activeDebug;
        activeDebug = null;

        SProgram contProg = new SProgramImpl("resume-prog");
        dbg.getInstructions().forEach(contProg::addInstruction);
        contProg.setFunctionLookup(program.getFunctionLookup());

        return new ProgramExecuterImpl(
                contProg,
                dbg.getContext(),
                dbg.getPc(),
                dbg.getTotalCycles()
        ).runWithReport();
    }

    @Override
    public ExecutionReport  stopDebugSession() {
        if (activeDebug != null) {
            activeDebug.stop();
            ExecutionReport report = activeDebug.buildFinalReport();
            activeDebug = null;
            return report;
        }
        return null;
    }

    @Override
    public boolean isDebugActive() {
        return activeDebug != null && !activeDebug.isFinished();
    }

    @Override
    public int getCurrentPc() {
        return (activeDebug != null) ? activeDebug.getPc() : -1;
    }

    @Override
    public ExecutionReport buildInitialReport() {
        return (activeDebug != null) ? activeDebug.buildInitialReport() : null;
    }



    //---Functions---
    @Override
    public List<String> getFunctionNames() {
        if (program == null) return List.of();

        var lookup = program.getFunctionLookup();

        return lookup.allFunctionNames().stream()
                .map(lookup::userStringOf)
                .toList();
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
