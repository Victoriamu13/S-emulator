package logic.domain.expand.instructionsExpanded.noJumpExpandedInstr;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.instructionsExpanded.InstructionExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bNoJumpInst.DecreaseInst;
import logic.domain.instructions.basic.bNoJumpInst.IncreaseInst;
import logic.domain.instructions.basic.bNoJumpInst.NeutralInst;
import logic.domain.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.domain.instructions.synthetic.sNoJumpInst.AssignmentInst;
import logic.domain.instructions.synthetic.sNoJumpInst.ZeroVariableInst;
import logic.domain.instructions.synthetic.sJumpInst.GoToLabelInst;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

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
