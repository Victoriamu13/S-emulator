package logic.domain.expand.instructionsExpanded.noJumpExpandedInstr;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.instructionsExpanded.InstructionExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bNoJumpInst.DecreaseInst;
import logic.domain.instructions.basic.bNoJumpInst.NeutralInst;
import logic.domain.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

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
