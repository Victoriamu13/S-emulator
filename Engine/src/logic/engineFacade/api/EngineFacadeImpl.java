package logic.engineFacade.api;
import logic.domain.architecture.ArchitectureAnalyzer;
import logic.domain.architecture.ArchitectureGen;
import logic.domain.execution.context.CurrentContext;
import logic.domain.execution.context.CurrentContextImpl;
import logic.domain.execution.executer.progExecuter.ProgramExecuterImpl;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.program.SProgramImpl;
import logic.domain.program.info.ExpandedProgramInfo;
import logic.domain.variable.SVarsType;
import logic.engineFacade.model.ArchitectureSummary;
import logic.engineFacade.model.debug.DebugSession;
import logic.engineFacade.model.LoadOutcome;
import logic.engineFacade.model.ExecutionReport;
import logic.domain.expand.expandProgram.DegreeCalculator;
import logic.engineFacade.model.InstructionDTO;
import logic.infrastructure.io.app.CurrentAppState;
import logic.infrastructure.io.xml.load.LoadResult;
import logic.infrastructure.io.xml.load.LoadService;
import logic.domain.program.SProgram;
import logic.domain.program.info.ProgramInfo;

import java.io.InputStream;
import java.util.*;

import static logic.engineFacade.api.EngineFacadeUtils.*;
import static logic.engineFacade.api.EngineFacadeUtils.toDto;

public class EngineFacadeImpl implements EngineFacade {
    private SProgram program;
    private SProgram currentProgram;
    private final LoadService loader = new LoadService();
    private DebugSession activeDebug;
    private  final Map<Integer, List<String>> cachedInputs = new HashMap<>();
    private Integer cachedMaxDegree = null;
    private Set<Integer> breakpoints = new HashSet<>();
    private ExecutionReport lastReport = null;


    private SProgram activeProgram() {
        return (currentProgram != null) ? currentProgram : program;
    }


    // ==== Load ====
    @Override
    public LoadOutcome loadProgram(InputStream inputStream) {
        try {
            LoadResult res = loader.loadFromXml(inputStream, new CurrentAppState());
            if (res.success() && res.program() != null) {
                this.program = res.program();
                this.currentProgram = program;
                resetExpansionCache();
                return LoadOutcome.ok();
            }
            return LoadOutcome.fail(res.errors());
        } catch (Exception e) {
            List<String> errors = new ArrayList<>();
            errors.add("Unexpected error while loading program from stream: " + e.getMessage());
            return LoadOutcome.fail(errors);
        }
    }



    @Override
    public String getProgramName() {return activeProgram() != null ? activeProgram().getName() : "";}

    @Override
    public SProgram getProgram() {return activeProgram();}



    // ==== Instruction Info ===
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
                if (ii.label() != null && !ii.label().isBlank()) labels.add(ii.label());
            });
        }
        return labels.stream().sorted(numericAwareComparator()).toList();
    }


    // ==== Expansion ====
    @Override
    public int getMaxExpansionDegree() {
        if (cachedMaxDegree != null) return cachedMaxDegree;
        cachedMaxDegree=DegreeCalculator.calculateMaxDegree(activeProgram());
        return cachedMaxDegree;
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


    // ==== Execution ====
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

        cachedInputs.put(degree,new ArrayList<>(rawValues));
        return parseInputValues(rawValues, required,inputsUsed);
    }


    @Override
    public List<String> getCachedInputValues(int degree) {
        List<String> inputs = getInputsUsed(degree);
        List<String> vals = cachedInputs.get(degree);
        if (vals == null || vals.size() != inputs.size()) {
            vals = new ArrayList<>();
            for (int i = 0; i < inputs.size(); i++) vals.add("0");
            cachedInputs.put(degree, vals);
        }
        return new ArrayList<>(vals);
    }

    @Override
    public List<String> getInputsUsed(int degree) {
        int maxDegree=getMaxExpansionDegree();
        int used=validDegree(degree,maxDegree);
        return getProgramInfo(activeProgram(),maxDegree,used).getInputsUsed();
    }

    @Override
    public List<String> loadInputVars(int degree) {
        List<String> inputs = getInputsUsed(degree);

        List<String> defaultValues = new java.util.ArrayList<>();
        for (int i = 0; i < inputs.size(); i++) {
            defaultValues.add("0");
        }
        cachedInputs.put(degree, defaultValues);
        return inputs;
    }

    @Override
    public ExecutionReport runWithReport(int degree,long... inputs) {
        int maxDegree=getMaxExpansionDegree();
        int used = validDegree(degree,maxDegree);
        SProgram materialized = materializeProgram(activeProgram(),used);

        ExecutionReport report = new ProgramExecuterImpl(materialized).runWithReport(breakpoints, inputs);
        this.lastReport=report;
        return report;
    }

    @Override
    public ExecutionReport getLastReport() { return lastReport; }

    // ==== Debug =====

    @Override
    public boolean startDebugSession(int degree, long... inputs) {
        activeDebug = null;
        int maxDegree=getMaxExpansionDegree();
        int used = validDegree(degree,maxDegree);
        SProgram prog = materializeProgram(activeProgram(),used);

        CurrentContext ctx = new CurrentContextImpl(inputs, prog.getFunctionLookup());
        List<SInstruction> instructions=prog.getInstructions();

        activeDebug=new DebugSession(instructions,ctx);
        activeDebug.setBreakpoints(new HashSet<>(this.breakpoints));
        this.lastReport = activeDebug.buildInitialReport();

        return true;
    }

    @Override
    public ExecutionReport stepOver() {
        ExecutionReport r = (activeDebug != null) ? activeDebug.step() : null;
        if (r != null) this.lastReport = r;
        return r;
    }

    @Override
    public ExecutionReport stepBack() {return (activeDebug != null) ? activeDebug.stepBack() : null;}

    @Override
    public ExecutionReport resume() {
        if (activeDebug == null) return null;
        ExecutionReport report = activeDebug.resumeUntilBreakpoint();
        this.lastReport = report;
        if (activeDebug.isFinished()) activeDebug = null;
        return report;
    }

    @Override
    public ExecutionReport  stopDebugSession() {
        if (activeDebug != null) {
            activeDebug.stop();
            ExecutionReport report = activeDebug.buildFinalReport();
            this.lastReport = report;
            activeDebug = null;
            return report;
        }
        return null;
    }

    @Override
    public boolean isDebugActive() {return activeDebug != null && !activeDebug.isFinished();}

    @Override
    public int getCurrentPc() {return (activeDebug != null) ? activeDebug.getPc() : -1;}

    @Override
    public ExecutionReport buildInitialReport() {return (activeDebug != null) ? activeDebug.buildInitialReport() : null;}


    // ==== Breakpoints ====
    @Override
    public Set<Integer> getBreakpoints() {
        if (activeDebug != null) return new HashSet<>(activeDebug.getBreakpoints());
        return new HashSet<>(breakpoints);
    }

    @Override
    public void toggleBreakpoint(int idx) {
        if (activeDebug != null) {
            activeDebug.toggleBreakpoint(idx);
        }
        if (breakpoints.contains(idx)) {
            breakpoints.remove(idx);
        } else {
            breakpoints.add(idx);
        }
    }


    @Override
    public void clearAllBreakpoints() {
        if (activeDebug != null) activeDebug.clearBreakpoints();
    }

    @Override
    public void setBreakpoints(Set<Integer> bps) {
        if (activeDebug != null) {
            activeDebug.setBreakpoints(bps);
        } else {
            this.breakpoints = new HashSet<>(bps);
            if (bps != null) this.breakpoints.addAll(bps);
        }
    }


    // ==== Functions ===
    @Override
    public List<String> getFunctionNames() {
        if (program == null) return List.of();
        var lookup = program.getFunctionLookup();
        return lookup.allFunctionNames().stream()
                .map(lookup::userStringOf)
                .toList();
    }

    @Override
    public LoadOutcome loadExistingProgram(SProgram program) {
        if (program == null) {
            List<String> errors = new ArrayList<>();
            errors.add("can't load program: program is null.");
            return LoadOutcome.fail(errors);
        }
        this.program=program;
        this.currentProgram = program;
        resetExpansionCache();
        return LoadOutcome.ok();
    }

    @Override
    public Map<String, ArchitectureSummary> getArchitectureSummary(int degree) {
        Map<String, ArchitectureSummary> result = new LinkedHashMap<>();
        SProgram program = activeProgram();
        if (program == null) return result;

        int maxDegree = getMaxExpansionDegree();
        int used = validDegree(degree, maxDegree);

        SProgram expandedProgram = materializeProgram(program, used);
        var info = getProgramInfo(program, maxDegree, used);
        int total = info.getInstructions().size();

        Map<ArchitectureGen, Integer> requiredMap = ArchitectureAnalyzer.countFromArchitecture(expandedProgram);
        for (ArchitectureGen gen : ArchitectureGen.values()) {
            int supported = requiredMap.getOrDefault(gen, 0);;
            result.put(gen.name(), new ArchitectureSummary(gen.name(), supported, total));
        }
        return result;
    }

    @Override
    public boolean isArchitectureCompatible(String architectureName, int degree) {
        if (program == null) return false;
        if (architectureName == null || architectureName.isBlank()) return false;

        ArchitectureGen selectedGen = ArchitectureGen.valueOf(architectureName);
        int maxDegree = getMaxExpansionDegree();
        int used = validDegree(degree, maxDegree);

        // Use expanded program for current degree
        SProgram expanded = materializeProgram(activeProgram(), used);
        if (expanded == null) return false;

        for (var inst : expanded.getInstructions()) {
            if (!selectedGen.getSupportedInstructions().contains(inst.getData())) {
                return false; // found unsupported instruction
            }
        }
        return true;
    }

}