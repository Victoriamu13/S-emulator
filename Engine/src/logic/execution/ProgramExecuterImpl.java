package logic.execution;

import logic.instructions.SInstruction;
import logic.label.SpecialLabels;
import logic.program.ProgramInfo;
import logic.program.ProgramInfoImpl;
import logic.program.SProgram;
import logic.label.SLabel;
import logic.variable.SVars;
import logic.variable.SVarsImpl;
import logic.variable.SVarsType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgramExecuterImpl implements ProgramExecuter {
    private final SProgram program;


    public ProgramExecuterImpl(SProgram program) {
        this.program = program;

    }

    @Override
    public long run(long... input) {

        CurrentContext context = new CurrentContextImpl();
        for (int i = 0; i < input.length; i++) {
            SVars xi = new SVarsImpl(SVarsType.INPUT, i + 1);
            context.updateVariable(xi, input[i]);
        }

        List<SInstruction> instructions = program.getInstructions();
        Map<String, Integer> labelIndex = new HashMap<>();

        for (int i = 0; i < instructions.size(); i++) {
            SLabel lbl = instructions.get(i).getLabel();
            if (lbl != null && !(lbl instanceof SpecialLabels)) {
                labelIndex.put(lbl.getLabelRepresentation(), i);
            }
        }

        int i = 0;
        while (i >= 0 && i < instructions.size()) {
            SInstruction currInstruction = instructions.get(i);
            SLabel nextLabel = currInstruction.executeOperarion(context);

            if(currInstruction.getLabel()==SpecialLabels.EXIT){
                nextLabel=SpecialLabels.EXIT;}

            if (nextLabel == SpecialLabels.EXIT) {
                break;
            } else if (nextLabel == SpecialLabels.EMPTY) {
                i++;
            } else {
                Integer index = labelIndex.get(nextLabel.getLabelRepresentation());
                if (index == null) {break;}
                i = index;
            }
        }
        SVars x1 = new SVarsImpl(SVarsType.INPUT, 1);
        context.updateVariable(SVars.RESULT, context.getVariableValue(x1));
        return context.getVariableValue(SVars.RESULT);
    }
}


