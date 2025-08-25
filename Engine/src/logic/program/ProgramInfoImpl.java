package logic.program;

import logic.instructions.*;
import logic.instructions.basic.bNoJumpInst.DecreaseInst;
import logic.instructions.basic.bNoJumpInst.IncreaseInst;
import logic.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.instructions.basic.bNoJumpInst.NeutralInst;
import logic.instructions.synthetic.sJumpInst.GoToLabelInst;
import logic.instructions.synthetic.sJumpInst.JumpEqualConstantInst;
import logic.instructions.synthetic.sJumpInst.JumpEqualVariableInst;
import logic.instructions.synthetic.sJumpInst.JumpZeroInst;
import logic.instructions.synthetic.sNoJumpInst.AssignmentInst;
import logic.instructions.synthetic.sNoJumpInst.ConstantAssignmentInst;
import logic.instructions.synthetic.sNoJumpInst.ZeroVariableInst;
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
            SLabel lbl=s.getLabel();
            int index=i+1;
            boolean synthetic=isSynthetic(s);
            String labelText=lbl.getLabelRepresentation();
            String commandText=toCommandText(s);
            int cycles=s.cycles();
            out.add(new InstructionInfoImpl(index,synthetic,labelText,commandText,cycles));
        }
        return out;
    }


    //Helper functions for getInstructions func

    public static String toCommandText(SInstruction inst) {
        SVars v = inst.getVariable();
        String var = (v != null) ? v.getRepresentation() : "";

        switch (inst) {
            //basic instructions
            case IncreaseInst inc -> {return String.format("%s <- %s + 1", var, var);}
            case DecreaseInst dec -> {return String.format("%s <- %s - 1", var, var);}
            case NeutralInst ntrl -> {return String.format("%s <- %s", var, var);}
            case JumpNotZeroInst jnz -> {
                SLabel target = jnz.getTargetLabel();
                String tgt = target.getLabelRepresentation();
                return String.format("IF %s != 0 GOTO %s", var, tgt);
            }

            //synthetic instructions
            case ZeroVariableInst zv -> { return String.format("%s <- 0", var); }
            case AssignmentInst asg -> {
                String vS=asg.getSourceVar().getRepresentation();
                return String.format("%s <- %s", var,vS );
            }
            case ConstantAssignmentInst kset -> {
                long valC=kset.getConstantValue();
                return String.format("%s <- %d", var,valC );
            }
            case JumpZeroInst jz -> {
                String target=jz.getTargetLabel().getLabelRepresentation();
                return String.format("IF %s = 0 GOTO %s", var,target );
            }
            case JumpEqualConstantInst jec -> {
                long valC=jec.getConstantValue();
                String target=jec.getTargetLabel().getLabelRepresentation();
                return String.format("IF %s = %d GOTO %s", var, valC, target);
            }
            case JumpEqualVariableInst jev -> {
                String varOt=jev.getOtherVar().getRepresentation();
                String target=jev.getTargetLabel().getLabelRepresentation();
                return String.format("IF %s = %s GOTO %s", var, varOt, target);
            }
            case GoToLabelInst go -> {
                String target=go.getTargetLabel().getLabelRepresentation();
                return String.format("GOTO %s", target);
            }

            default -> {return inst.getName();}
        }
    }

    private static boolean isSynthetic(SInstruction inst) {
        return (inst instanceof ZeroVariableInst) ||
                (inst instanceof AssignmentInst) ||
                (inst instanceof ConstantAssignmentInst) ||
                (inst instanceof JumpZeroInst) ||
                (inst instanceof JumpEqualConstantInst) ||
                (inst instanceof JumpEqualVariableInst) ||
                (inst instanceof GoToLabelInst);
    }
}
