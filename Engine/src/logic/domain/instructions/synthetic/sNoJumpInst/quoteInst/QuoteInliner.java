package logic.domain.instructions.synthetic.sNoJumpInst.quoteInst;

import logic.domain.instructions.SInstruction;

import java.util.ArrayList;
import java.util.List;

public class QuoteInliner {

    public List<SInstruction> inline(List<SInstruction> quotedProgram,QuoteContext qctx) {
        List<SInstruction> out = new ArrayList<>();
        QuoteMapper mapper = new QuoteMapper(quotedProgram, qctx);

        out.addAll(mapper.buildArgumentAssignments());

        for (SInstruction inst : quotedProgram) {
            out.add(mapper.remapInstruction(inst));
        }

        out.add(mapper.buildResultAssignment());

        return out;
    }
}
