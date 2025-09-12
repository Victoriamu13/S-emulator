package logic.domain.expand.instructionsExpanded.jumpExpandedInst;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.instructionsExpanded.InstructionExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bNoJumpInst.DecreaseInst;
import logic.domain.instructions.basic.bNoJumpInst.NeutralInst;
import logic.domain.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.domain.instructions.synthetic.sNoJumpInst.AssignmentInst;
import logic.domain.instructions.synthetic.sJumpInst.GoToLabelInst;
import logic.domain.instructions.synthetic.sJumpInst.JumpEqualConstantInst;
import logic.domain.instructions.synthetic.sJumpInst.JumpZeroInst;
import logic.domain.label.SLabel;
import logic.domain.variable.SVars;

import java.util.ArrayList;
import java.util.List;

public class JumpEqlConstExpander implements InstructionExpander {

    @Override
    public List<SInstruction> expand(SInstruction inst, ExpansionContext ctx) {
        JumpEqualConstantInst je = (JumpEqualConstantInst) inst;
        SVars var = inst.getVariable();
        SVars y = SVars.RESULT;
        long k  =  je.getConstantValue();
        SLabel L = inst.getTargetLabel();
        SLabel L1 = ctx.newFreeLabel();
        SVars z1 = ctx.newWorkVar();
        SVars dummy= ctx.newWorkVar();

        List<SInstruction> out = new ArrayList<>();
        out.add(new AssignmentInst(z1,var,inst.getLabel()));

        for(int i=0;i<k;i++){
            out.add(new JumpZeroInst(z1,L1));
            out.add(new DecreaseInst(z1));
        }

        out.add(new JumpNotZeroInst(z1,L1));
        out.add(new GoToLabelInst(dummy,L));
        out.add(new NeutralInst(y,L1));
        return out;
    }
}
