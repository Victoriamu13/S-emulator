package logic.io.xml.build;

import logic.instructions.*;
import logic.instructions.basic.DecreaseInst;
import logic.instructions.basic.IncreaseInst;
import logic.instructions.basic.jumpInstructions.JumpNotZeroInst;
import logic.instructions.basic.NeutralInst;
import logic.io.xml.dto.RawInstructions;
import logic.label.SLabel;
import logic.program.SProgram;
import logic.program.SProgramImpl;
import logic.variable.SVars;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static logic.io.xml.build.BuildUtils.*;
import static logic.io.xml.build.BuildUtils.buildTargetLabel;

public class XmlProgramBuilder {

    public SProgram build(String programName, List<RawInstructions> raw) {
        SProgramImpl program = new SProgramImpl(programName.trim());

        for (RawInstructions r : raw) {
            InstructionData instName = InstructionData.valueOf(r.name().trim().toUpperCase(Locale.ROOT));
            SLabel lineLabel = buildLineLabel(r.labelText());
            SVars var = buildVar(r.varText());
            Map<ArgumentData, String> args = buildArgs(r.args());

            SInstruction inst = switch (instName) {
                case INCREASE -> new IncreaseInst(var,lineLabel);
                case DECREASE -> new DecreaseInst(var,lineLabel);
                case NEUTRAL-> new NeutralInst(var,lineLabel);
                case JUMP_NOT_ZERO -> {
                    SLabel target = buildTargetLabel(args);
                    yield new JumpNotZeroInst(var, target, lineLabel);
                }
            };

            program.addInstruction(inst);
        }

        return program;
    }
}

