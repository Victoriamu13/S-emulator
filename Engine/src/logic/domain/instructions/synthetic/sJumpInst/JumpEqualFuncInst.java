package logic.domain.instructions.synthetic.sJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import logic.infrastructure.io.xml.parser.composition.ComposeArgument;

import java.util.List;

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
        return SpecialLabels.EMPTY;
    }

}
