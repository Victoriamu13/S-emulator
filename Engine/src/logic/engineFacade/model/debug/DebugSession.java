package logic.engineFacade.model.debug;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.SInstruction;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import logic.engineFacade.model.ExecutionReport;
import logic.system.user.credits.CreditManager;

import java.util.*;

public class DebugSession {
    private final List<SInstruction> instructions;
    private final CurrentContext ctx;
    private final String username;
    private int pc;
    private long totalCycles;
    private boolean finished;
    private Map<SVars, Long> lastSnapshot = new HashMap<>();
    private final Deque<DebugState> debugHistory = new ArrayDeque<>();
    private  Set<Integer> breakpoints = new HashSet<>();


    // ==== Constructors ====
    public DebugSession(List<SInstruction> instructions, CurrentContext ctx) {
        this(null, instructions, ctx);
    }

    public DebugSession(String username,List<SInstruction> instructions, CurrentContext ctx) {
        this.username = username;
        this.instructions = instructions;    //instructions
        this.ctx = ctx;                      //variables context
        this.pc = 0;                         //program counter
        this.totalCycles = 0;                //total cycles
        this.finished = false;               //finished flag
        this.lastSnapshot = new HashMap<>(ctx.snapshot());  //initial snapshot
        debugHistory.push(new DebugState(pc,totalCycles,new HashMap<>(lastSnapshot)));  //initial state in history
    }

    // ==== Main API ====
    public ExecutionReport step(){
        if( finished || (pc >= instructions.size())){
            finished = true;
            return buildReport(Set.of());
        }
        debugHistory.push(new DebugState(pc, totalCycles, new HashMap<>(lastSnapshot)));  //save state before step

        SInstruction inst=instructions.get(pc);  //fetch instruction
        totalCycles+=inst.cycles();               //update cycles

        // Decrease credits according to cycles
        CreditManager.consumeCredit(username, inst.cycles());
        if (CreditManager.getCredits(username) <= 0) {
            finished = true;
            totalCycles = -1; // signal OUT_OF_CREDITS
            return buildReport(Set.of());
        }

        SLabel next=inst.executeOperation(ctx);  //execute instruction
        if(next== SpecialLabels.EXIT){
            finished=true;
        }else if(next==SpecialLabels.EMPTY){
            pc++;                                //move to next instruction
        }
        else{  //find label
            String target=next.getLabelRepresentation();;
            int newPc=-1;
            for(int i=0;i<instructions.size();i++){
                if(instructions.get(i).getLabel().getLabelRepresentation().equals(target)){
                    newPc=i;
                    break;
                }
            }
            pc = (newPc != -1) ? newPc : pc + 1;
        }

        if (pc >= instructions.size()) finished = true;  //check if finished

        // Compare snapshots to find changed variables
        Map<SVars, Long> currentSnap = ctx.snapshot();
        Set<String> changedVars = new HashSet<>();
        for (var entry : currentSnap.entrySet()) {
            SVars var = entry.getKey();
            long newVal = entry.getValue();
            long oldVal = lastSnapshot.getOrDefault(var, Long.MIN_VALUE);
            if (newVal != oldVal) changedVars.add(var.getRepresentation());

        }
        lastSnapshot = new HashMap<>(currentSnap);  //update last snapshot
        return buildReport(changedVars);
    }


    public ExecutionReport stepBack(){
        if (debugHistory.isEmpty()){
            return buildReport(Set.of());  //no history to step back to
        }

        //pop last state
        DebugState prevState = debugHistory.pop();
        this.pc = prevState.pc();
        this.totalCycles = prevState.totalCycles();
        ctx.restoreSnapshot(prevState.snapshot());
        lastSnapshot=new HashMap<>(prevState.snapshot());
        return buildReport(Set.of());
    }


    // ==== Debug report builders ====

    private ExecutionReport buildReport(Set<String> changedVars) {
        long yVal = ctx.getVariableValue(SVars.RESULT);
        Map<String, Long> finalVars = snapshotAsStrings(ctx.snapshot());
        return new ExecutionReport(yVal,changedVars, finalVars, totalCycles);
    }

    public ExecutionReport buildInitialReport() {
        return buildReport(Set.of());
    }

    public ExecutionReport buildFinalReport() {
        return buildReport(Set.of());
    }

    private Map<String, Long> snapshotAsStrings(Map<SVars, Long> snap) {
        Map<String, Long> out = new HashMap<>();
        snap.forEach((v, val) -> out.put(v.getRepresentation(), val));
        return out;
    }


    // ==== Breakpoints API ====

    public void toggleBreakpoint(int index) {
        if (breakpoints.contains(index)) breakpoints.remove(index);
        else breakpoints.add(index);
    }

    public void clearBreakpoints() { breakpoints.clear(); }

    public void setBreakpoints(Set<Integer> bps) {
        breakpoints.clear();
        if (bps != null) breakpoints.addAll(bps);
    }

    public Set<Integer> getBreakpoints() {
        return Collections.unmodifiableSet(breakpoints);
    }


    // ==== Resume/Stop ====

    public ExecutionReport resumeUntilBreakpoint() {
        final boolean skipFirst = breakpoints.contains(pc);
        boolean skipped = false;

        while (!finished && pc < instructions.size()) {
            ExecutionReport report = step();

            if (breakpoints.contains(pc)) {
                if (skipFirst && !skipped) {
                    skipped = true;   // Skip the first breakpoint hit
                } else {
                    return report;
                }
            }
        }
        return buildFinalReport();
    }

    public void stop() {this.finished = true;}

    // ==== Getters ====

    public boolean isFinished() {return finished;}
    public CurrentContext getContext() {return ctx;}
    public int getPc() {return pc;}
    public List<SInstruction> getInstructions(){return instructions;}
}
