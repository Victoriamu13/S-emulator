package logic.expand.instructionsExpanded.noJumpExpandedInstr;

import logic.expand.expandProgram.ExpansionContext;
import logic.expand.instructionsExpanded.InstructionExpander;
import logic.instructions.SInstruction;
import logic.instructions.basic.bNoJumpInst.DecreaseInst;
import logic.instructions.basic.bNoJumpInst.IncreaseInst;
import logic.instructions.basic.bNoJumpInst.NeutralInst;
import logic.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.instructions.synthetic.sNoJumpInst.AssignmentInst;
import logic.instructions.synthetic.sNoJumpInst.ZeroVariableInst;
import logic.instructions.synthetic.sJumpInst.GoToLabelInst;
import logic.label.SLabel;
import logic.variable.SVars;

import java.util.ArrayList;
import java.util.List;

public final class AssignmentExpander implements InstructionExpander {

    @Override
    public List<SInstruction> expand(SInstruction inst, ExpansionContext ctx){
        AssignmentInst a = (AssignmentInst) inst;
        SVars var   = inst.getVariable();
        SVars varP  = a.getSourceVar();
        SVars z1 = ctx.newWorkVar();
        SVars dummy= ctx.newWorkVar();
        SLabel L1 = ctx.newFreeLabel();
        SLabel L2 = ctx.newFreeLabel();
        SLabel L3 = ctx.newFreeLabel();

        List<SInstruction> out = new ArrayList<>();

        out.add(new ZeroVariableInst(var, inst.getLabel()));

        out.add(new JumpNotZeroInst(varP, L1));
        out.add(new GoToLabelInst(dummy, L3));
        out.add(new DecreaseInst(varP,L1));
        out.add(new IncreaseInst(z1));
        out.add(new JumpNotZeroInst(varP, L1));

        out.add(new DecreaseInst(z1, L2));
        out.add(new IncreaseInst(var));
        out.add(new IncreaseInst(varP));
        out.add(new JumpNotZeroInst(z1, L2));

        out.add(new NeutralInst(var, L3));

        return out;
    }
}
