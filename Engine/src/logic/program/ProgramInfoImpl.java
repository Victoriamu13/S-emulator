package logic.program;

import logic.instructions.*;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;
import logic.variable.SVarsType;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public class ProgramInfoImpl implements ProgramInfo {
    private final SProgram program;

    public ProgramInfoImpl(SProgram program) {
        this.program = program;
    }

    @Override
    public String getName(){
        return program.getName();
    }

    @Override
    public int getNumberOfInstructions(){
        return program.getInstructions().size();
    }

    @Override
    public List<String> getInputsUsed(){
        LinkedHashSet<String> inputs = new LinkedHashSet<>();
        for (SInstruction inst : program.getInstructions()) {
            SVars var=inst.getVariable();
            if(var!=null && var.getType()== SVarsType.INPUT){
                inputs.add(var.getRepresentation());
            }
        }
        return new ArrayList<>(inputs);
    }

    @Override
    public List<String> getLabelsUsed(){
        LinkedHashSet<String> labels = new LinkedHashSet<>();
        boolean hasExit=false;
        for(SInstruction inst : program.getInstructions()){
            SLabel lbl=inst.getLabel();
            if(lbl==null || lbl== SpecialLabels.EMPTY){continue;}
            if(lbl==SpecialLabels.EXIT){
                hasExit=true;
            }else{
                labels.add(lbl.getLabelRepresentation());
            }
        }
        List<String> out = new ArrayList<>(labels);
        if(hasExit){out.add("EXIT");}
        return out;
    }

    @Override
    public List<InstructionInfo> getInstructions(){
        List<SInstruction>inst = program.getInstructions();
        List<InstructionInfo> out = new ArrayList<>(inst.size());

        for(int i=0; i<inst.size(); i++){
            SInstruction s = inst.get(i);
            int index=i+1;
            boolean synthetic=false;
            String labelText=labelToText(s.getLabel());
            String commandText=toCommandText(s);
            int cycles=s.cycles();
            out.add(new InstructionInfoImpl(index,synthetic,labelText,commandText,cycles));
        }
        return out;
    }


    //Helper functions for getInstructions func

    private static String labelToText(SLabel lbl) {
        return lbl.getLabelRepresentation();
    }

    private static String toCommandText(SInstruction inst) {
        SVars v = inst.getVariable();
        String var = (v != null) ? v.getRepresentation() : "";

        switch (inst) {
            case IncreaseInstruction increaseInstruction -> {
                return String.format("%s <- %s + 1", var, var);
            }
            case DecreaseInstruction decreaseInstruction -> {
                return String.format("%s <- %s - 1", var, var);
            }
            case NoOpInstruction noOpInstruction -> {
                return String.format("%s <- %s", var, var);
            }
            case JumpNotZeroInstruction jumpNotZeroInstruction -> {
                SLabel target = jumpNotZeroInstruction.getJumpLabel();
                String tgt = target.getLabelRepresentation();
                return String.format("IF %s != 0 GOTO %s", var, tgt);
            }
            default -> {return inst.getName();}
        }
    }
}
