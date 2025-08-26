package logic.expand.instructionsExpanded.jumpExpandedInst;

import logic.expand.expandProgram.ExpansionContext;
import logic.expand.instructionsExpanded.InstructionExpander;
import logic.instructions.SInstruction;
import logic.instructions.basic.bNoJumpInst.NeutralInst;
import logic.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.instructions.synthetic.sJumpInst.GoToLabelInst;
import logic.label.SLabel;
import logic.variable.SVars;

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
