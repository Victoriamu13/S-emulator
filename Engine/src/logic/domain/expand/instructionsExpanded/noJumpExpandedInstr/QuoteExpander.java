package logic.domain.expand.instructionsExpanded.noJumpExpandedInstr;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.instructionsExpanded.InstructionExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bNoJumpInst.NeutralInst;
import logic.domain.instructions.synthetic.sNoJumpInst.quoteInst.QuoteContext;
import logic.domain.instructions.synthetic.sNoJumpInst.quoteInst.QuoteInliner;
import logic.domain.instructions.synthetic.sNoJumpInst.quoteInst.QuoteInst;
import logic.domain.label.SpecialLabels;
import logic.domain.program.functions.FunctionLookup;
import logic.domain.variable.SVars;
import logic.infrastructure.io.xml.build.BuildUtils;

import java.util.ArrayList;
import java.util.List;

public class QuoteExpander implements InstructionExpander {
    private final FunctionLookup functions;

    public QuoteExpander(FunctionLookup functions){
        this.functions=functions;
    }

    @Override
    public List<SInstruction> expand(SInstruction inst, ExpansionContext ctx){
        QuoteInst quoteInst=(QuoteInst) inst;

        List<SInstruction> qBody=functions.bodyOf(quoteInst.getFunctionName());

        List<SVars> originArgs=quoteInst.getArguments()
                .stream().map(BuildUtils::buildVar).toList();

        QuoteContext qctx = new QuoteContext(originArgs, quoteInst.getVariable(), ctx, qBody);

        QuoteInliner inliner =new QuoteInliner();
        List<SInstruction> inlinedInstructions=inliner.inline(qBody,qctx);

        List<SInstruction>out=new ArrayList<>();
        if(inst.getLabel()!= SpecialLabels.EMPTY){
            out.add(new NeutralInst(ctx.newWorkVar(), inst.getLabel()));
        }
        out.addAll(inlinedInstructions);
        return out;
    }
}
