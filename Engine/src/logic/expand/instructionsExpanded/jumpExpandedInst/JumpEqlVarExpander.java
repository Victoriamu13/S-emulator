package logic.expand.instructionsExpanded.jumpExpandedInst;

import logic.expand.expandProgram.ExpansionContext;
import logic.expand.instructionsExpanded.InstructionExpander;
import logic.instructions.InstructionData;
import logic.instructions.SInstruction;
import logic.instructions.basic.bNoJumpInst.DecreaseInst;
import logic.instructions.basic.bNoJumpInst.NeutralInst;
import logic.instructions.synthetic.sNoJumpInst.AssignmentInst;
import logic.instructions.synthetic.sJumpInst.GoToLabelInst;
import logic.instructions.synthetic.sJumpInst.JumpEqualVariableInst;
import logic.instructions.synthetic.sJumpInst.JumpZeroInst;
import logic.label.SLabel;
import logic.variable.SVars;

import java.util.ArrayList;
import java.util.List;

public class JumpEqlVarExpander implements InstructionExpander {

    @Override
    public List<SInstruction> expand(SInstruction inst, ExpansionContext ctx){
        JumpEqualVariableInst je = (JumpEqualVariableInst) inst;
        SVars var  = inst.getVariable();
        SVars varP = je.getOtherVar();
        SLabel L = inst.getTargetLabel();
        SVars y=SVars.RESULT;
        SVars z1  = ctx.newWorkVar();
        SVars z2  = ctx.newWorkVar();
        SVars dummy= ctx.newWorkVar();
        SLabel L1= ctx.newFreeLabel();
        SLabel L2 = ctx.newFreeLabel();
        SLabel L3= ctx.newFreeLabel();

        List<SInstruction> out = new ArrayList<>();
        out.add(new AssignmentInst(z1,var,inst.getLabel()));
        out.add(new AssignmentInst(z2,varP));

        out.add(new JumpZeroInst(z1,L3,L2));
        out.add(new JumpZeroInst(z2,L1));
        out.add(new DecreaseInst(z1));
        out.add(new DecreaseInst(z2));
        out.add(new GoToLabelInst(dummy,L2));

        out.add(new JumpZeroInst(z2,L,L3));
        out.add(new NeutralInst(y,L1));
        return out;
    }
}
