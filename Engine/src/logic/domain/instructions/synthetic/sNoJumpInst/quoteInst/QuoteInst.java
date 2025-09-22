package logic.domain.instructions.synthetic.sNoJumpInst.quoteInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;

import java.util.List;

public class QuoteInst extends AbstractInstruction {
    private final String functionName;
    private final List<String> functionArgs;

    public QuoteInst(SVars targetVar,String functionName,List<String>arguments){
        this(targetVar,functionName,arguments, SpecialLabels.EMPTY);
    }

    public QuoteInst(SVars targetVar, String functionName, List<String>arguments, SLabel label){
        super(InstructionData.QUOTE,targetVar,label);
        this.functionName=functionName;
        this.functionArgs=arguments;
    }

    public String getFunctionName(){
        return functionName;
    }

    public List<String> getArguments(){
        return functionArgs;
    }

    @Override
    public SLabel executeOperation(CurrentContext context){
        return SpecialLabels.EMPTY;
    }

}
