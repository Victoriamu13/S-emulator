package logic.domain.instructions.synthetic.sNoJumpInst.quoteInst;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.instructions.SInstruction;
import logic.domain.variable.SVars;

import java.util.List;

import static logic.domain.program.info.ProgramInfoUtils.inputsUsed;

public final class QuoteContext {
    private final List<SVars> originArgs;
    private final List<String> originInputs;
    private final SVars targetVar;
    private final ExpansionContext ctx;

    public QuoteContext(List<SVars> originArgs, SVars targetVar, ExpansionContext ctx,List<SInstruction> quotedProgram) {
        this.originArgs = originArgs;
        this.originInputs=inputsUsed(quotedProgram);
        this.targetVar = targetVar;
        this.ctx = ctx;
    }

    public List<SVars> originArgs() { return originArgs; }
    public List<String> originInputs() { return originInputs; }
    public SVars targetVar() { return targetVar; }
    public ExpansionContext ctx() { return ctx; }
}
