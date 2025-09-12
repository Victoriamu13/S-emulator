package logic.domain.expand.instructionsExpanded.jumpExpandedInst;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.instructionsExpanded.InstructionExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bNoJumpInst.NeutralInst;
import logic.domain.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.domain.instructions.synthetic.sJumpInst.GoToLabelInst;
import logic.domain.label.SLabel;
import logic.domain.variable.SVars;

import java.util.ArrayList;
import java.util.List;

public class JumpZeroExpander implements InstructionExpander {

    @Override
    public List<SInstruction> expand(SInstruction inst, ExpansionContext ctx){
        SVars var = inst.getVariable();
        SLabel L = inst.getTargetLabel();
        SLabel L1 = ctx.newFreeLabel();
        SVars y   = SVars.RESULT;
        SVars dummy= ctx.newWorkVar();

        List<SInstruction> out = new ArrayList<>();
        out.add(new JumpNotZeroInst(var, L1, inst.getLabel()));
        out.add(new GoToLabelInst(dummy,L));
         out.add(new NeutralInst(y, L1));

         return out;

    }
}
