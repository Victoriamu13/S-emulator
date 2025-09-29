package logic.domain.instructions.synthetic.sJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.execution.executer.FunctionExecuter;
import logic.domain.execution.executer.FunctionResult;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import logic.infrastructure.io.xml.parser.composition.ComposeArgument;

import java.util.List;
import java.util.Map;

public class JumpEqualFuncInst extends AbstractInstruction {
    private final String functionName;
    private final List<ComposeArgument> functionArgs;
    private final SLabel targetLabel;

    public JumpEqualFuncInst(SVars variable, String functionName, List<ComposeArgument> functionArgs, SLabel targetLabel){
        this(variable,functionName,functionArgs,targetLabel, SpecialLabels.EMPTY);
    }

    public JumpEqualFuncInst(SVars variable, String functionName, List<ComposeArgument> functionArgs, SLabel targetLabel, SLabel label){
        super(logic.domain.instructions.data.InstructionData.JUMP_EQUAL_FUNCTION,variable,label);
        this.functionName=functionName;
        this.functionArgs=functionArgs;
        this.targetLabel=targetLabel;
    }

    public String getFunctionName(){ return functionName;}
    public List<ComposeArgument> getFunctionArgs(){ return functionArgs; }
    public SLabel getTargetLabel() { return targetLabel;}

    @Override
    public SLabel executeOperation(CurrentContext context){
        FunctionResult fr = FunctionExecuter.evaluateFunctionCall(context, functionName, functionArgs);
        long varVal = context.getVariableValue(getVariable());
        context.addCycles(fr.cycles() + 5);
        return (varVal == fr.value()) ? targetLabel : SpecialLabels.EMPTY;
    }

    @Override
    public SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap) {
        SVars newVar     = varMap.getOrDefault(getVariable(), getVariable());
        SLabel newLabel   = labelMap.getOrDefault(getLabel(), getLabel());
        SLabel newTarget  = labelMap.getOrDefault(getTargetLabel(), getTargetLabel());
        return new JumpEqualFuncInst(newVar, functionName, functionArgs, newTarget, newLabel);
    }
}
