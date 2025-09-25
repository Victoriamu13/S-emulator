package logic.domain.expand.instructionsExpanded.jumpExpandedInst;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.instructionsExpanded.InstructionExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.synthetic.sJumpInst.JumpEqualFuncInst;
import logic.domain.expand.functionCall.FunctionCallExpander;

import java.util.List;

public class JumpEqualFuncExpander implements InstructionExpander {

    @Override
    public List<SInstruction> expand(SInstruction inst, ExpansionContext ctx){
        if (!(inst instanceof JumpEqualFuncInst jumpEq)) return List.of(inst);
        FunctionCallExpander mapper = new FunctionCallExpander(ctx, jumpEq);
        return mapper.expandJumpEqualFunc();
    }
}
