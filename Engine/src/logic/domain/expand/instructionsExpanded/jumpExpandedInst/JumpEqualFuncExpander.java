package logic.domain.expand.instructionsExpanded.jumpExpandedInst;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.instructionsExpanded.InstructionExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.synthetic.sJumpInst.JumpEqualFuncInst;
import logic.domain.instructions.synthetic.sJumpInst.JumpEqualVariableInst;
import logic.domain.instructions.synthetic.sNoJumpInst.quoteInst.QuoteInst;
import logic.domain.label.SLabel;
import logic.domain.variable.SVars;

import java.util.ArrayList;
import java.util.List;

public class JumpEqualFuncExpander implements InstructionExpander {

    @Override
    public List<SInstruction> expand(SInstruction inst, ExpansionContext ctx){

        JumpEqualFuncInst jef=(JumpEqualFuncInst)inst;
        List<SInstruction> out=new ArrayList<>();

        SVars z1=ctx.newWorkVar();
        QuoteInst quote=new QuoteInst(z1,jef.getFunctionName(),jef.getFunctionArgs(),inst.getLabel());
        out.add(quote);

        SLabel target = jef.getTargetLabel();
        JumpEqualVariableInst jump = new JumpEqualVariableInst(jef.getVariable(), z1, target);
        out.add(jump);

        return out;
    }
}
