package logic.domain.execution.executer;

import logic.engineFacade.model.ExecutionReport;
import logic.domain.execution.context.CurrentContext;
import logic.domain.execution.context.CurrentContextImpl;
import logic.domain.instructions.SInstruction;
import logic.domain.label.SpecialLabels;
import logic.domain.program.SProgram;
import logic.domain.label.SLabel;
import logic.domain.variable.SVars;

import java.util.*;

public class ProgramExecuterImpl implements ProgramExecuter {
    private final SProgram program;


    public ProgramExecuterImpl(SProgram program) {
        this.program = program;

    }

    @Override
    public long run(long... inputs) {
        return runWithReport(inputs).yValue();
    }


    @Override
    public ExecutionReport runWithReport(long... inputs) {
        CurrentContext context = new CurrentContextImpl(inputs,program.getFunctionLookup());

        List<SInstruction> instructions = program.getInstructions();
        Map<String, Integer> labelIndex = new HashMap<>();
        for (int i = 0; i < instructions.size(); i++) {
            SLabel lbl = instructions.get(i).getLabel();
            if (lbl.isNumberLabel()) {
                labelIndex.put(lbl.getLabelRepresentation(), i);
            }
        }

        long totalCycles = 0L;
        int instIndex = 0; // instruction index

        while (instIndex >= 0 && instIndex < instructions.size()) {
            SInstruction inst = instructions.get(instIndex);
            totalCycles+=inst.cycles();
            SLabel nextLabel=inst.executeOperation(context);

            if(nextLabel==SpecialLabels.EXIT){
                break;
            }else if(nextLabel==SpecialLabels.EMPTY){
                instIndex++;
            }else{
                instIndex= labelIndex.get(nextLabel.getLabelRepresentation());
            }
        }
        long yVal = context.getVariableValue(SVars.RESULT);
        Map<String,Long> finalVarsValues=orderVarsForReport(context.snapshot());
        return new ExecutionReport(yVal, finalVarsValues, totalCycles);
    }


    //-----helper funcs-----
    private static Map<String, Long> orderVarsForReport(Map<SVars, Long> snap) {
        LinkedHashMap<String,Long> out=new LinkedHashMap<>();

        out.put("y", snap.getOrDefault(SVars.RESULT, 0L));

        TreeMap<Integer,Long> xMap=new TreeMap<>();
        TreeMap<Integer,Long> zMap=new TreeMap<>();

        for(Map.Entry<SVars,Long> e:snap.entrySet()) {
            SVars v = e.getKey();    //x,z
            long val = e.getValue();

            switch(v.getType()){
                case INPUT->{
                    String rep=v.getRepresentation();
                    int n=Integer.parseInt(rep.substring(1));
                    xMap.put(n,val);
                }
                case WORK->{
                    String rep=v.getRepresentation();
                    int n=Integer.parseInt(rep.substring(1));
                    zMap.put(n,val);
                }
                default -> {}
            }
        }
        for(Map.Entry<Integer,Long> e:xMap.entrySet()) {
            out.put("x"+e.getKey(),e.getValue());
        }
        for(Map.Entry<Integer,Long> e:zMap.entrySet()) {
            out.put("z"+e.getKey(),e.getValue());
        }
        return out;
    }

}


