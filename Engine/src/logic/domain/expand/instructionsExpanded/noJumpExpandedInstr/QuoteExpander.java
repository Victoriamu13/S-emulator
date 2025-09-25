package logic.domain.expand.instructionsExpanded.noJumpExpandedInstr;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.instructionsExpanded.InstructionExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.synthetic.sNoJumpInst.QuoteInst;
import logic.domain.expand.functionCall.FunctionCallExpander;
import logic.domain.program.functions.FunctionLookup;

import java.util.List;

public class QuoteExpander implements InstructionExpander {
    private final FunctionLookup functions;

    public QuoteExpander(FunctionLookup functions){
        this.functions=functions;
    }

    @Override
    public List<SInstruction> expand(SInstruction inst, ExpansionContext ctx) {
        if (!(inst instanceof QuoteInst quote)) return List.of(inst);
        FunctionCallExpander mapper = new FunctionCallExpander(ctx, quote);
        return mapper.expandQuote();
    }

}
