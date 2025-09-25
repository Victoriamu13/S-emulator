package logic.domain.instructions.synthetic.sNoJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.execution.executer.FunctionExecuter;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import logic.infrastructure.io.xml.parser.composition.ComposeArgument;

import java.util.List;
import java.util.Map;

public class QuoteInst extends AbstractInstruction {
    private final String functionName;
    private final List<ComposeArgument> functionArgs;

    public QuoteInst(SVars targetVar,String functionName,List<ComposeArgument>arguments){
        this(targetVar,functionName,arguments, SpecialLabels.EMPTY);
    }

    public QuoteInst(SVars targetVar, String functionName, List<ComposeArgument>arguments, SLabel label){
        super(InstructionData.QUOTE,targetVar,label);
        this.functionName=functionName;
        this.functionArgs=arguments;
    }

    public String getFunctionName(){
        return functionName;
    }

    public List<ComposeArgument> getArguments(){
        return functionArgs;
    }


    @Override
    public SLabel executeOperation(CurrentContext context){
        FunctionExecuter.assignFunctionResult(context, getVariable(), functionName, functionArgs);
        return SpecialLabels.EMPTY;
    }

    @Override
    public SInstruction remap(Map<SVars,SVars> varMap, Map<SLabel,SLabel> labelMap) {
        SVars newTarget = varMap.getOrDefault(getVariable(), getVariable());
        SLabel newLabel = labelMap.getOrDefault(getLabel(), getLabel());
        return new QuoteInst(newTarget, functionName, functionArgs, newLabel);
    }
}
