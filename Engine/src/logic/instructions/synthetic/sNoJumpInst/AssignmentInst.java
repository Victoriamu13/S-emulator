package logic.instructions.synthetic.sNoJumpInst;

import logic.execution.CurrentContext;
import logic.instructions.AbstractInstruction;
import logic.instructions.InstructionData;
import logic.label.SLabel;
import logic.label.SpecialLabels;
import logic.variable.SVars;
import logic.variable.SVarsImpl;
import logic.variable.SVarsType;

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
