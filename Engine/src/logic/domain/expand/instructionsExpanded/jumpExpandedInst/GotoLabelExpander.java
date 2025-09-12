package logic.domain.expand.instructionsExpanded.jumpExpandedInst;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.instructionsExpanded.InstructionExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bNoJumpInst.IncreaseInst;
import logic.domain.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.domain.label.SLabel;
import logic.domain.variable.SVars;

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
