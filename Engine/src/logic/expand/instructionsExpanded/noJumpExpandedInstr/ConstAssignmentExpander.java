package logic.expand.instructionsExpanded.noJumpExpandedInstr;

import logic.expand.expandProgram.ExpansionContext;
import logic.expand.instructionsExpanded.InstructionExpander;
import logic.instructions.InstructionData;
import logic.instructions.SInstruction;
import logic.instructions.basic.bNoJumpInst.IncreaseInst;
import logic.instructions.synthetic.sNoJumpInst.ConstantAssignmentInst;
import logic.instructions.synthetic.sNoJumpInst.ZeroVariableInst;
import logic.variable.SVars;

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
