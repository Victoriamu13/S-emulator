package logic.domain.execution.executer;

import logic.domain.execution.context.CurrentContext;
import logic.domain.execution.context.CurrentContextImpl;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.synthetic.sJumpInst.JumpEqualFuncInst;
import logic.domain.instructions.synthetic.sNoJumpInst.QuoteInst;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.program.functions.FunctionLookup;
import logic.domain.variable.SVars;
import logic.infrastructure.io.xml.build.BuildUtils;
import logic.infrastructure.io.xml.parser.composition.ComposeArgument;
import logic.infrastructure.io.xml.parser.composition.FuncCallArgument;
import logic.infrastructure.io.xml.parser.composition.VarArgument;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionExecuter { //Run functions as "Black Box"

    public static long evaluateArgument(ComposeArgument arg, CurrentContext ctx,FunctionLookup fnLookup){
        if(arg instanceof VarArgument var){
            SVars v = BuildUtils.buildVar(var.getName());
           return ctx.getVariableValue(v);
        }

        if(arg instanceof FuncCallArgument func) {
            List<SInstruction> funcInstructions = ctx.getFunctionLookup().bodyOf(func.getFunctionName());
            List<Long> childVals = func.getArguments().stream()
                    .map(a -> evaluateArgument(a,ctx, fnLookup))
                    .toList();
            return executeFunctionBody(funcInstructions, childVals, fnLookup);
        }
        return 0L;
    }

    public static long evaluateFunctionCall(CurrentContext ctx, String fnName, List<ComposeArgument> args) {
        FunctionLookup fnLookup = ctx.getFunctionLookup();

        List<Long> argVals = args.stream()
                .map(a -> evaluateArgument(a, ctx, fnLookup))
                .toList();

        List<SInstruction> fnBody = fnLookup.bodyOf(fnName);
        return executeFunctionBody(fnBody, argVals, fnLookup);
    }


    public static long executeFunctionBody(List<SInstruction> fnBody, List<Long> args, FunctionLookup fnLookup){
        long[] xs = new long[args.size()];
        for (int i = 0; i < args.size(); i++) xs[i] = args.get(i);

        CurrentContext local = new CurrentContextImpl(xs,fnLookup);

        Map<String,Integer> labelIndex = new HashMap<>();
        for (int i = 0; i < fnBody.size(); i++) {
            SLabel lbl = fnBody.get(i).getLabel();
            if (lbl.isNumberLabel()) {
                labelIndex.put(lbl.getLabelRepresentation(), i);
            }
        }

        int pc = 0;
        while (pc >= 0 && pc < fnBody.size()) {
            SInstruction inst = fnBody.get(pc);
            SLabel next;

            if (inst instanceof QuoteInst q) {
                List<Long> qArgs = q.getArguments().stream()
                        .map(a -> evaluateArgument(a, local, fnLookup))
                        .toList();
                List<SInstruction> qBody = fnLookup.bodyOf(q.getFunctionName());
                long res = executeFunctionBody(qBody, qArgs, fnLookup);
                local.updateVariable(q.getVariable(), res);
                next = SpecialLabels.EMPTY;
            }
            else if (inst instanceof JumpEqualFuncInst jef) {
                List<Long> jArgs = jef.getFunctionArgs().stream()
                        .map(a -> evaluateArgument(a, local, fnLookup))
                        .toList();
                List<SInstruction> jBody = fnLookup.bodyOf(jef.getFunctionName());
                long res = executeFunctionBody(jBody, jArgs, fnLookup);
                long vVal = local.getVariableValue(jef.getVariable());
                next = (vVal == res) ? jef.getTargetLabel() : SpecialLabels.EMPTY;
            }
            else {
                next = inst.executeOperation(local);
            }

            if (next == SpecialLabels.EXIT) break;
            if (next.isNumberLabel()) {
                pc = labelIndex.get(next.getLabelRepresentation());
            } else {
                pc++;
            }
        }

        return local.getVariableValue(SVars.RESULT);
    }


    public static void assignFunctionResult(CurrentContext ctx, SVars target, String fnName, List<ComposeArgument> args) {
        FunctionLookup fnLookup = ctx.getFunctionLookup();

        List<Long> argVals = args.stream()
                .map(a -> evaluateArgument(a, ctx, fnLookup))
                .toList();

        List<SInstruction> fnBody = fnLookup.bodyOf(fnName);
        long res = executeFunctionBody(fnBody, argVals, fnLookup);
        ctx.updateVariable(target, res);
    }
}
