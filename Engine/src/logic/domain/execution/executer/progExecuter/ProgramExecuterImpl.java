package logic.domain.execution.executer.progExecuter;

import logic.domain.execution.executer.funcExecuter.FunctionExecuter;
import logic.domain.execution.executer.funcExecuter.FunctionResult;
import logic.domain.execution.utils.ExecutionUtils;
import logic.domain.execution.utils.LabelUtils;
import logic.domain.instructions.synthetic.sJumpInst.JumpEqualFuncInst;
import logic.domain.instructions.synthetic.sNoJumpInst.QuoteInst;
import logic.engineFacade.model.ExecutionReport;
import logic.domain.execution.context.CurrentContext;
import logic.domain.execution.context.CurrentContextImpl;
import logic.domain.instructions.SInstruction;
import logic.domain.label.SpecialLabels;
import logic.domain.program.SProgram;
import logic.domain.label.SLabel;
import logic.domain.variable.SVars;

import java.util.*;

import static logic.domain.execution.utils.ExecutionUtils.orderVarsForReport;

public class ProgramExecuterImpl implements ProgramExecuter {
    private final SProgram program;           // program to run
    private CurrentContext externalContext;   // resume debugging from context
    private int startPc = 0;                 // resume from pc
    private long initialCycles = 0;          // resume cycles

    public ProgramExecuterImpl(SProgram program) {
        this.program = program;
    }

    public ProgramExecuterImpl(SProgram program, CurrentContext ctx, int startPc, long initialCycles) {
        this.program = program;                 // program
        this.externalContext = ctx;             // injected context
        this.startPc = startPc;                 // injected pc
        this.initialCycles = initialCycles;     // injected cycles
    }

    @Override
    public ExecutionReport runWithReport(Set<Integer>breakpoints,long... inputs) {
        // Decide context/pc based on resume vs fresh run

        final CurrentContext context = (externalContext != null)
                ? externalContext
                : new CurrentContextImpl(inputs, program.getFunctionLookup());

        int instIndex = (externalContext != null) ? startPc : 0;       // start location
        long totalCycles = initialCycles;                       // cycles so far

        List<SInstruction> instructions = program.getInstructions();
        Map<String, Integer> labelIndex = LabelUtils.indexNumericLabels(instructions);

        while (instIndex >= 0 && instIndex < instructions.size()) {

            if (breakpoints.contains(instIndex)) {
                // Pause & snapshot
                long yVal = context.getVariableValue(SVars.RESULT);
                Map<String, Long> finalVarsValues = orderVarsForReport(context.snapshot());
                this.startPc = instIndex;         // remember pc for next step
                this.externalContext = context;  // keep context alive
                return new ExecutionReport(yVal, Set.of(), finalVarsValues, totalCycles);
            }

            SInstruction inst = instructions.get(instIndex);   // current instruction
            totalCycles+=inst.cycles();                        // add instr cycles
            SLabel next;

            if (inst instanceof QuoteInst q) {
                FunctionResult fr = FunctionExecuter.evaluateFunctionCall(
                        context, q.getFunctionName(), q.getArguments());          // run quote
                context.updateVariable(q.getVariable(), fr.value());              // assign
                totalCycles += fr.cycles();                                       // add nested cycles
                next = SpecialLabels.EMPTY;                                       // continue
            }
            else if (inst instanceof JumpEqualFuncInst jef) {
                FunctionResult fr = FunctionExecuter.evaluateFunctionCall(        // run func
                        context, jef.getFunctionName(), jef.getFunctionArgs());
                long vVal = context.getVariableValue(jef.getVariable());         // compare
                totalCycles += fr.cycles();
                next = (vVal == fr.value()) ? jef.getTargetLabel() : SpecialLabels.EMPTY;
            }
            else {
                next = inst.executeOperation(context);
            }
            // next pc
            int newPc = ExecutionUtils.safeJump(instIndex, next, labelIndex);
            if (newPc == Integer.MIN_VALUE) break;                              // EXIT
            instIndex = newPc;
        }
        long yVal = context.getVariableValue(SVars.RESULT);    //final Y
        Map<String,Long> finalVarsValues=orderVarsForReport(context.snapshot());   //final vars
        return new ExecutionReport(yVal,Set.of(), finalVarsValues, totalCycles);
    }



}


