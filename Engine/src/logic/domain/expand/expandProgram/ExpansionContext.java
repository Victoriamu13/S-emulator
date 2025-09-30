package logic.domain.expand.expandProgram;

import logic.domain.instructions.SInstruction;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.instructions.synthetic.sJumpInst.GoToLabelInst;
import logic.domain.instructions.synthetic.sNoJumpInst.QuoteInst;
import logic.domain.expand.functionCall.FunctionCallExpander;
import logic.domain.label.SLabel;
import logic.domain.label.SLabelImpl;
import logic.domain.program.SProgram;
import logic.domain.program.functions.FunctionLookup;
import logic.domain.variable.SVars;
import logic.domain.variable.SVarsImpl;
import logic.domain.variable.SVarsType;
import logic.infrastructure.io.xml.parser.composition.ComposeArgument;
import logic.infrastructure.io.xml.parser.composition.FuncCallArgument;
import logic.infrastructure.io.xml.parser.composition.VarArgument;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ExpansionContext {
    private int workVarCounter   = 1;
    private int freeLabelCounter = 1;

    private final Set<String> usedWorkNames  = new HashSet<>();
    private final Set<String> usedLabelNames = new HashSet<>();

    private FunctionLookup functions;

    public static ExpansionContext seedFrom(SProgram program) {
        ExpansionContext ctx = new ExpansionContext();
        ctx.markUsedFromProgram(program.getInstructions());
        ctx.withFunctionLookup(program.getFunctionLookup());
        return ctx;
    }

    public void markUsedFromProgram(List<SInstruction> program) {
        for (SInstruction ins : program) {

            String lblRep = ins.getLabel().getLabelRepresentation();
            if (lblRep!=null && !lblRep.equals("EXIT") && !lblRep.isEmpty()) {
                usedLabelNames.add(lblRep);
                int n = Integer.parseInt(lblRep.substring(1));
                if (n >= freeLabelCounter) freeLabelCounter = n + 1;
            }

            if (ins instanceof GoToLabelInst) { //Avoid dummy variable
              continue;
            } else {
                SVars var = ins.getVariable();
                if (var != null) {
                    String varRep = var.getRepresentation();
                    if (varRep.startsWith("z") || varRep.startsWith("Z")) {
                        usedWorkNames.add(varRep);
                        int n = Integer.parseInt(varRep.substring(1));
                        if (n >= workVarCounter) workVarCounter = n + 1;
                    }
                }
            }
        }
    }

    public SVars newWorkVar() {
        while (true) {
            SVars v = new SVarsImpl(SVarsType.WORK, workVarCounter++);
            String rep = v.getRepresentation();  // "zN"
            if (usedWorkNames.add(rep)) return v;
        }
    }

    public SLabel newFreeLabel() {
        while (true) {
            SLabel lbl = new SLabelImpl(freeLabelCounter++);
            String rep = lbl.getLabelRepresentation();
            if (usedLabelNames.add(rep)) return lbl;
        }
    }

    public ExpansionContext withFunctionLookup(FunctionLookup f) { this.functions = f; return this; }
    public FunctionLookup getFunctionLookup() { return functions; }

    public List<SInstruction> lookupFunctionBody(String fnName) {
        return functions.bodyOf(fnName);
    }

    public SVars lookupFunctionResult(String fnName) {
        return SVars.RESULT;
    }


    public SVars resolveArgument(ComposeArgument arg, List<SInstruction> out) {
        if (arg instanceof VarArgument var) {
            String name = var.getName();

            if (name.startsWith("x")) {
                return new SVarsImpl(SVarsType.INPUT, Integer.parseInt(name.substring(1)));
            }
            else if (name.startsWith("z")) {
                return new SVarsImpl(SVarsType.WORK, Integer.parseInt(name.substring(1)));
            }
            else if (name.equals("y")) {
                return new SVarsImpl(SVarsType.RESULT, 0);
            }
        }

        if (arg instanceof FuncCallArgument func) {
            SVars tmp = newWorkVar();

            QuoteInst innerQuote = new QuoteInst(tmp, func.getFunctionName(), func.getArguments());
            FunctionCallExpander innerMapper = new FunctionCallExpander(this, innerQuote);
            out.addAll(innerMapper.expandQuote());

            return tmp;
        }
        return null;
    }
}
