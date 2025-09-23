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
import logic.infrastructure.io.xml.parser.composition.ComposeArgument;
import logic.infrastructure.io.xml.parser.composition.FuncCallArgument;
import logic.infrastructure.io.xml.parser.composition.VarArgument;

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
        List<SInstruction>  prelude = new ArrayList<>();
        List<SVars> resolvedArgs  = new ArrayList<>();

        // Recursively resolve arguments
        for (ComposeArgument arg : quoteInst.getArguments()) {
            SVars v = resolveArg(arg, ctx, prelude);
            resolvedArgs.add(v);
        }

        List<SInstruction> qBody=functions.bodyOf(quoteInst.getFunctionName());
        QuoteContext qctx = new QuoteContext(resolvedArgs, quoteInst.getVariable(), ctx, qBody);

        QuoteInliner inliner =new QuoteInliner();
        List<SInstruction> inlinedInstructions=inliner.inline(qBody,qctx);

        List<SInstruction>out=new ArrayList<>();
        if(inst.getLabel()!= SpecialLabels.EMPTY){
            out.add(new NeutralInst(ctx.newWorkVar(), inst.getLabel()));
        }
        out.addAll(prelude);
        out.addAll(inlinedInstructions);
        return out;
    }

    private SVars resolveArg(ComposeArgument arg, ExpansionContext ctx, List<SInstruction> prelude){
        if(arg instanceof VarArgument varArg){
            return BuildUtils.buildVar(varArg.getName());

        } else if (arg instanceof FuncCallArgument funcArg) {
            String funcName = funcArg.getFunctionName();
            List<ComposeArgument> funcArgs = funcArg.getArguments();
            List<SVars> resolvedFuncArgs = new ArrayList<>();

            for (ComposeArgument fArg : funcArgs) {
                SVars v = resolveArg(fArg, ctx, prelude);
                resolvedFuncArgs.add(v);
            }

            SVars resultVar = ctx.newWorkVar();
            QuoteInst qInst = new QuoteInst(resultVar, funcName, funcArgs);
            List<SInstruction> qExpanded = this.expand(qInst, ctx);
            prelude.addAll(qExpanded);
            return resultVar;
        }
        return null;
    }
}
