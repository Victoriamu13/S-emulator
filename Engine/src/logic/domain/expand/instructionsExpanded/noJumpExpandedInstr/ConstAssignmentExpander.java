package logic.domain.expand.instructionsExpanded.noJumpExpandedInstr;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.instructionsExpanded.InstructionExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bNoJumpInst.IncreaseInst;
import logic.domain.instructions.synthetic.sNoJumpInst.ConstantAssignmentInst;
import logic.domain.instructions.synthetic.sNoJumpInst.ZeroVariableInst;
import logic.domain.variable.SVars;

import java.util.ArrayList;
import java.util.List;

public final class ConstAssignmentExpander implements InstructionExpander {

    @Override
    public List<SInstruction> expand(SInstruction inst, ExpansionContext ctx) {
        ConstantAssignmentInst c = (ConstantAssignmentInst) inst;
        SVars var = inst.getVariable();
        long k  = c.getConstantValue();

        List<SInstruction> out = new ArrayList<>();
        out.add(new ZeroVariableInst(var, inst.getLabel()));

        for (long i = 0; i < k; i++) out.add(new IncreaseInst(var));
        return out;
    }
}
