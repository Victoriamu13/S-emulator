package logic.engineFacade.model.debug;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.SInstruction;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import logic.engineFacade.model.ExecutionReport;

import java.util.*;


public class DebugSession {
    private final List<SInstruction> instructions;
    private final CurrentContext ctx;
    private int pc;
    private long totalCycles;
    private boolean finished;
    private Map<SVars, Long> lastSnapshot = new HashMap<>();
    private final Deque<DebugState> debugHistory = new ArrayDeque<>();


    public DebugSession(List<SInstruction> instructions, CurrentContext ctx) {
        this.instructions = instructions;
        this.ctx = ctx;
        this.pc = 0;
        this.totalCycles = 0;
        this.finished = false;
        this.lastSnapshot = new HashMap<>(ctx.snapshot());

        debugHistory.push(new DebugState(pc,totalCycles,new HashMap<>(lastSnapshot)));
    }

    public ExecutionReport step(){
        if( finished || (pc >= instructions.size())){
            finished = true;
            return buildReport(Set.of());
        }
        debugHistory.push(new DebugState(pc, totalCycles, new HashMap<>(lastSnapshot)));

        SInstruction inst=instructions.get(pc);
        totalCycles+=inst.cycles();

        SLabel next=inst.executeOperation(ctx);
        if(next== SpecialLabels.EXIT){
            finished=true;
        }else if(next==SpecialLabels.EMPTY){
            pc++;
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

        if (pc >= instructions.size()) {
            finished = true;
        }

        Map<SVars, Long> currentSnap = ctx.snapshot();
        Set<String> changedVars = new HashSet<>();
        for (var entry : currentSnap.entrySet()) {
            SVars var = entry.getKey();
            long newVal = entry.getValue();
            long oldVal = lastSnapshot.getOrDefault(var, Long.MIN_VALUE);

            if (newVal != oldVal) {
                changedVars.add(var.getRepresentation());
            }
        }
        lastSnapshot = new HashMap<>(currentSnap);
        return buildReport(changedVars);
    }

    public ExecutionReport stepBack(){
        if (debugHistory.isEmpty()){
            return buildReport(Set.of());
        }

        DebugState prevState = debugHistory.pop();
        this.pc = prevState.pc();
        this.totalCycles = prevState.totalCycles();

        ctx.restoreSnapshot(prevState.snapshot());

        lastSnapshot=new HashMap<>(prevState.snapshot());

        return buildReport(Set.of());
    }


    private ExecutionReport buildReport(Set<String> changedVars) {
        long yVal = ctx.getVariableValue(SVars.RESULT);
        Map<String, Long> finalVars = snapshotAsStrings(ctx.snapshot());
        return new ExecutionReport(yVal,changedVars, finalVars, totalCycles);
    }

    private Map<String, Long> snapshotAsStrings(Map<SVars, Long> snap) {
        Map<String, Long> out = new HashMap<>();
        snap.forEach((v, val) -> out.put(v.getRepresentation(), val));
        return out;
    }

    public ExecutionReport buildInitialReport() {
        long yVal = ctx.getVariableValue(SVars.RESULT);
        Map<String, Long> finalVars = snapshotAsStrings(ctx.snapshot());
        return new ExecutionReport(yVal, Set.of(), finalVars, totalCycles);
    }

    public ExecutionReport buildFinalReport() {
        long yVal = ctx.getVariableValue(SVars.RESULT);
        Map<String, Long> finalVars = snapshotAsStrings(ctx.snapshot());
        return new ExecutionReport(yVal, Set.of(), finalVars, totalCycles);
    }

    public void stop() {this.finished = true;}
    public boolean isFinished() {return finished;}
    public CurrentContext getContext() {return ctx;}
    public int getPc() {return pc;}
    public long getTotalCycles(){return totalCycles;}
    public List<SInstruction> getInstructions(){return instructions;}
}
