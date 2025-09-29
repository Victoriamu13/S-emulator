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

    public static FunctionResult  evaluateArgument(ComposeArgument arg, CurrentContext ctx,FunctionLookup fnLookup){
        if(arg instanceof VarArgument var){
            SVars v = BuildUtils.buildVar(var.getName());
            long val = ctx.getVariableValue(v);
            return new FunctionResult(val, 0);
        }

        if(arg instanceof FuncCallArgument func) {
            long totalCycles = 0;
            List<FunctionResult> childResults = func.getArguments().stream()
                    .map(a -> evaluateArgument(a, ctx, fnLookup))
                    .toList();

            totalCycles += childResults.stream().mapToLong(FunctionResult::cycles).sum();

            List<Long> childVals = childResults.stream()
                    .map(FunctionResult::value)
                    .toList();

            FunctionResult fr = executeFunctionBody(fnLookup.bodyOf(func.getFunctionName()), childVals, fnLookup);
            return new FunctionResult(fr.value(), totalCycles + fr.cycles());
        }
        return new FunctionResult(0, 0);
    }

    public static FunctionResult evaluateFunctionCall(CurrentContext ctx, String fnName, List<ComposeArgument> args) {
        FunctionLookup fnLookup = ctx.getFunctionLookup();

        long totalCycles = 0;
        List<FunctionResult> argResults = args.stream()
                .map(a -> evaluateArgument(a, ctx, fnLookup))
                .toList();

        totalCycles += argResults.stream().mapToLong(FunctionResult::cycles).sum();

        List<Long> argVals = argResults.stream()
                .map(FunctionResult::value)
                .toList();

        FunctionResult fr = executeFunctionBody(fnLookup.bodyOf(fnName), argVals, fnLookup);
        return new FunctionResult(fr.value(), totalCycles + fr.cycles());
    }



    public static FunctionResult executeFunctionBody(List<SInstruction> fnBody, List<Long> args, FunctionLookup fnLookup){
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
                List<FunctionResult> qArgs = q.getArguments().stream()
                        .map(a -> evaluateArgument(a, local, fnLookup))
                        .toList();

                long argCycles = qArgs.stream().mapToLong(FunctionResult::cycles).sum();

                List<Long> argVals = qArgs.stream().map(FunctionResult::value).toList();

                FunctionResult fr = executeFunctionBody(
                        fnLookup.bodyOf(q.getFunctionName()), argVals, fnLookup);

                local.updateVariable(q.getVariable(), fr.value());
                local.addCycles(q.cycles() +argCycles+ fr.cycles());
                next = SpecialLabels.EMPTY;
            }
            else if (inst instanceof JumpEqualFuncInst jef) {
                List<FunctionResult> jArgs = jef.getFunctionArgs().stream()
                        .map(a -> evaluateArgument(a, local, fnLookup))
                        .toList();

                long argCycles = jArgs.stream().mapToLong(FunctionResult::cycles).sum();
                List<Long> argVals = jArgs.stream().map(FunctionResult::value).toList();
                FunctionResult fr = executeFunctionBody(
                        fnLookup.bodyOf(jef.getFunctionName()), argVals, fnLookup);

                long vVal = local.getVariableValue(jef.getVariable());
                local.addCycles(jef.cycles() + argCycles+fr.cycles());
                next = (vVal == fr.value()) ? jef.getTargetLabel() : SpecialLabels.EMPTY;
            }
            else {
                local.addCycles(inst.cycles());
                next = inst.executeOperation(local);
            }

            if (next == SpecialLabels.EXIT) break;
            if (next.isNumberLabel()) {
                pc = labelIndex.get(next.getLabelRepresentation());
            } else {
                pc++;
            }
        }

        return new FunctionResult(local.getVariableValue(SVars.RESULT), local.getCycles());
    }


    public static void assignFunctionResult(CurrentContext ctx, SVars target, String fnName, List<ComposeArgument> args) {
        FunctionLookup fnLookup = ctx.getFunctionLookup();
        FunctionResult fr = evaluateFunctionCall(ctx, fnName, args);
        ctx.updateVariable(target, fr.value());
    }

}
