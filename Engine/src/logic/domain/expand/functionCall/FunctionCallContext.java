package logic.domain.expand.functionCall;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.variable.SVars;
import logic.infrastructure.io.xml.parser.composition.ComposeArgument;

import java.util.ArrayList;
import java.util.List;

public final class FunctionCallContext {
    private final List<ComposeArgument> originArgs;
    private final List<SVars> resolvedArgs;
    private final List<String> originInputs;
    private final SVars targetVar;
    private final ExpansionContext ctx;

    public FunctionCallContext(List<ComposeArgument> originArgs, List<SVars> resolvedArgs,
                               SVars targetVar, ExpansionContext ctx) {
        this.originArgs = originArgs;
        this.resolvedArgs = (resolvedArgs == null) ? List.of() : List.copyOf(resolvedArgs);
        this.originInputs= buildParamNames(this.originArgs.size());
        this.targetVar = targetVar;
        this.ctx = ctx;
    }

    private static List<String> buildParamNames(int k) {
        List<String> out = new ArrayList<>(k);
        for (int i = 1; i <= k; i++) out.add("x" + i);
        return out;
    }

    public List<ComposeArgument> originArgs() { return originArgs; }
    public List<SVars> resolvedArgs() { return resolvedArgs; }
    public List<String> originInputs() { return originInputs; }
    public SVars targetVar() { return targetVar; }
    public ExpansionContext ctx() { return ctx; }
}
