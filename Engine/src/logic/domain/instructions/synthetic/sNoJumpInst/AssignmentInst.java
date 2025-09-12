package logic.domain.instructions.synthetic.sNoJumpInst;

import logic.domain.execution.context.CurrentContext;
import logic.domain.instructions.AbstractInstruction;
import logic.domain.instructions.data.InstructionData;
import logic.domain.label.SLabel;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import logic.domain.variable.SVarsImpl;
import logic.domain.variable.SVarsType;

public class AssignmentInst extends AbstractInstruction {
    private final SVars sourceVar;

    public AssignmentInst(SVars targetVar, SVars sourceVar) {
        this(targetVar,sourceVar, SpecialLabels.EMPTY);
    }

    public AssignmentInst(SVars targetVar, SVars sourceVar, SLabel label) {
        super(InstructionData.ASSIGNMENT,targetVar,label);
        this.sourceVar = sourceVar;
    }

    @Override
    public SLabel executeOperation(CurrentContext context){
        SVars targetVar=getVariable();
        SVars sourceVar=this.sourceVar;
        SVars workVar=new SVarsImpl(SVarsType.WORK,1);

        long targetVal=0;
        long sourceVal= context.getVariableValue(sourceVar);
        long workVal= context.getVariableValue(workVar);

        while(sourceVal!=0){
            sourceVal--;
            workVal++;
        }

        while(workVal!=0){
            targetVal++;
            sourceVal++;
            workVal--;
        }

        context.updateVariable(targetVar,targetVal);
        context.updateVariable(sourceVar,sourceVal);
        context.updateVariable(workVar,workVal);

        return SpecialLabels.EMPTY;
    }

    public SVars getSourceVar() { return sourceVar; }
}
