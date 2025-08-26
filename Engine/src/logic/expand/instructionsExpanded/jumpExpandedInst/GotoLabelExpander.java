package logic.expand.instructionsExpanded.jumpExpandedInst;

import logic.expand.expandProgram.ExpansionContext;
import logic.expand.instructionsExpanded.InstructionExpander;
import logic.instructions.SInstruction;
import logic.instructions.basic.bNoJumpInst.IncreaseInst;
import logic.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.label.SLabel;
import logic.variable.SVars;

import java.util.ArrayList;
import java.util.List;

public final class GotoLabelExpander implements InstructionExpander {

    @Override
    public List<SInstruction> expand(SInstruction inst, ExpansionContext ctx){
        SVars z1=ctx.newWorkVar();
        SLabel L=inst.getTargetLabel();
        List<SInstruction> out=new ArrayList<>();

        out.add(new IncreaseInst(z1,inst.getLabel()));
        out.add(new JumpNotZeroInst(z1,L));
        return out;
    }
}
