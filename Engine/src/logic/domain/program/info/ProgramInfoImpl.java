package logic.domain.program.info;

import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bNoJumpInst.DecreaseInst;
import logic.domain.instructions.basic.bNoJumpInst.IncreaseInst;
import logic.domain.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.domain.instructions.basic.bNoJumpInst.NeutralInst;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.instructions.synthetic.sJumpInst.*;
import logic.domain.instructions.synthetic.sNoJumpInst.AssignmentInst;
import logic.domain.instructions.synthetic.sNoJumpInst.ConstantAssignmentInst;
import logic.domain.instructions.synthetic.sNoJumpInst.ZeroVariableInst;
import logic.domain.instructions.synthetic.sNoJumpInst.QuoteInst;
import logic.domain.label.SLabel;
import logic.domain.program.SProgram;
import logic.domain.program.functions.FunctionsUtils;
import logic.domain.variable.SVars;

import java.util.ArrayList;
import java.util.List;

public class ProgramInfoImpl implements ProgramInfo {
    private final SProgram program;
    public ProgramInfoImpl(SProgram program) {
        this.program = program;
    }

    @Override public String getName(){
        return program.getName();
    }
    @Override public int getNumberOfInstructions(){
        return program.getInstructions().size();
    }
    @Override public List<String> getInputsUsed(){return ProgramInfoUtils.inputsUsed(program.getInstructions());}
    @Override public List<String> getLabelsUsed(){return ProgramInfoUtils.labelsUsed(program.getInstructions());}
    @Override public List<String> getVariablesUsed(){return ProgramInfoUtils.variablesUsed(program.getInstructions());}

    @Override
    public List<InstructionInfo> getInstructions(){
        List<SInstruction> inst = program.getInstructions();
        List<InstructionInfo> out = new ArrayList<>(inst.size());

        for(int i=0; i<inst.size(); i++){
            SInstruction s = inst.get(i);
            out.add(ProgramInfoUtils.toInfo(s, i+1, null)); // fullCommand=null => uses toCommandText(s)
        }
        return out;
    }

    //Helper functions for getInstructions func

    public static String toCommandText(SInstruction inst) {
        SVars v = inst.getVariable();
        String var = v.getRepresentation();

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
            case QuoteInst quote->{
                String funcName = quote.getFunctionName();
                String args = FunctionsUtils.argsToString(quote.getArguments());
                return String.format("%s <- (%s%s%s)", var, funcName, args.isEmpty() ? "" : ",", args);
            }
            case JumpEqualFuncInst jef->{
                String target=jef.getTargetLabel().getLabelRepresentation();
                String funcName = jef.getFunctionName();
                String args = FunctionsUtils.argsToString(jef.getFunctionArgs());
                return String.format("IF %s = (%s%s%s) GOTO %s",
                        var, funcName, args.isEmpty() ? "" : ",", args, target);
            }

            default -> {return inst.getName();}
        }
    }

    public static boolean isSynthetic(SInstruction inst) {
        return (inst instanceof ZeroVariableInst) ||
                (inst instanceof AssignmentInst) ||
                (inst instanceof ConstantAssignmentInst) ||
                (inst instanceof JumpZeroInst) ||
                (inst instanceof JumpEqualConstantInst) ||
                (inst instanceof JumpEqualVariableInst) ||
                (inst instanceof GoToLabelInst)||
                (inst instanceof QuoteInst)||
                (inst instanceof JumpEqualFuncInst);
    }

}
