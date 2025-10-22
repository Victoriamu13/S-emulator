package logic.domain.execution.executer.funcExecuter;

import logic.domain.execution.context.CurrentContext;
import logic.domain.execution.context.CurrentContextImpl;
import logic.domain.execution.utils.ExecutionUtils;
import logic.domain.execution.utils.LabelUtils;
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

import java.util.List;
import java.util.Map;

public class FunctionExecuter { //Run functions as "Black Box"

    // Evaluate a single ComposeArgument into (value,cycles)
    public static FunctionResult evaluateArgument(ComposeArgument arg, CurrentContext ctx, FunctionLookup fnLookup){
        if(arg instanceof VarArgument var){
            SVars v = BuildUtils.buildVar(var.getName());   // resolve variable name → SVars
            long val = ctx.getVariableValue(v);            // read runtime value
            return new FunctionResult(val, 0);       // reading vars costs 0 cycles
        }

        if(arg instanceof FuncCallArgument func) {
            // Evaluate child arguments first

            List<FunctionResult> childResults = func.getArguments().stream()
                    .map(a -> evaluateArgument(a, ctx, fnLookup))
                    .toList();

            long childCycles = childResults.stream().mapToLong(FunctionResult::cycles).sum();
            List<Long> childVals = childResults.stream().map(FunctionResult::value).toList();

            // Execute the inner function body as black box
            FunctionResult fr = executeFunctionBody(fnLookup.bodyOf(func.getFunctionName()), childVals, fnLookup);
            return new FunctionResult(fr.value(), ExecutionUtils.accumulateCycles(childCycles, fr.cycles()));
        }
        return new FunctionResult(0, 0);
    }


    public static FunctionResult evaluateFunctionCall(CurrentContext ctx, String fnName, List<ComposeArgument> args) {
        FunctionLookup fnLookup = ctx.getFunctionLookup();   // repository

        List<FunctionResult> argResults = args.stream()
                .map(a -> evaluateArgument(a, ctx, fnLookup))
                .toList();

        long argsCycles = argResults.stream().mapToLong(FunctionResult::cycles).sum();

        List<Long> argVals = argResults.stream()
                .map(FunctionResult::value)
                .toList();

        FunctionResult fr = executeFunctionBody(fnLookup.bodyOf(fnName), argVals, fnLookup);
        return new FunctionResult(fr.value(), ExecutionUtils.accumulateCycles(argsCycles, fr.cycles()));
    }


    // Execute function body instructions with local context
    public static FunctionResult executeFunctionBody(List<SInstruction> fnBody, List<Long> args, FunctionLookup fnLookup){
        long[] xs = new long[args.size()];
        for (int i = 0; i < args.size(); i++) xs[i] = args.get(i);

        CurrentContext local = new CurrentContextImpl(xs,fnLookup);
        Map<String,Integer> labelIndex = LabelUtils.indexNumericLabels(fnBody);// L# → index

        int pc = 0;             // program counter
        while (pc >= 0 && pc < fnBody.size()) {
            SInstruction inst = fnBody.get(pc);   // current instruction
            SLabel next;                          // default next label

            // Add instruction intrinsic cycles
            local.addCycles(inst.cycles());

            if (inst instanceof QuoteInst q) {
                // Evaluate QUOTE arguments

                List<FunctionResult> qArgs = q.getArguments().stream()
                        .map(a -> evaluateArgument(a, local, fnLookup))
                        .toList();

                long argCycles = qArgs.stream().mapToLong(FunctionResult::cycles).sum();
                List<Long> argVals = qArgs.stream().map(FunctionResult::value).toList();

                // Run quoted function
                FunctionResult fr = executeFunctionBody(
                        fnLookup.bodyOf(q.getFunctionName()), argVals, fnLookup);

                local.updateVariable(q.getVariable(), fr.value());
                local.addCycles(ExecutionUtils.accumulateCycles(argCycles, fr.cycles()));
                next = SpecialLabels.EMPTY;
            }
            else if (inst instanceof JumpEqualFuncInst jef) {
                // Evaluate JEF arguments

                List<FunctionResult> jArgs = jef.getFunctionArgs().stream()
                        .map(a -> evaluateArgument(a, local, fnLookup))
                        .toList();

                long argCycles = jArgs.stream().mapToLong(FunctionResult::cycles).sum();
                List<Long> argVals = jArgs.stream().map(FunctionResult::value).toList();

                FunctionResult fr = executeFunctionBody(
                        fnLookup.bodyOf(jef.getFunctionName()), argVals, fnLookup);

                long vVal = local.getVariableValue(jef.getVariable());

                local.addCycles(ExecutionUtils.accumulateCycles(argCycles, fr.cycles()));  // add nested cycles
                next = (vVal == fr.value()) ? jef.getTargetLabel() : SpecialLabels.EMPTY;
            }
            else {
                next = inst.executeOperation(local);
            }

            // Compute next pc
            pc = ExecutionUtils.safeJump(pc, next, labelIndex);
            if (pc == Integer.MIN_VALUE) break;
        }

        return new FunctionResult(local.getVariableValue(SVars.RESULT), local.getCycles());
    }

}
