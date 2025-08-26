package logic.expand.instructionsExpanded.noJumpExpandedInstr;

import logic.expand.expandProgram.ExpansionContext;
import logic.expand.instructionsExpanded.InstructionExpander;
import logic.instructions.SInstruction;
import logic.instructions.basic.bNoJumpInst.DecreaseInst;
import logic.instructions.basic.bNoJumpInst.NeutralInst;
import logic.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;

import java.util.ArrayList;
import java.util.List;

public final class ZeroVariableExpander implements InstructionExpander {

    @Override
    public List<SInstruction> expand(SInstruction inst, ExpansionContext ctx){
        SVars var=inst.getVariable();
        SLabel L1=ctx.newFreeLabel();
        List<SInstruction>out=new ArrayList<>();

        if (inst.getLabel() != SpecialLabels.EMPTY) {
            out.add(new NeutralInst(ctx.newWorkVar(), inst.getLabel())); //Save original label
        }
        //Expansion commands
        out.add(new DecreaseInst(var, L1));
        out.add(new JumpNotZeroInst(var, L1));
        return out;
    }
}
