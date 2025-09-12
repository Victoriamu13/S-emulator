package logic.infrastructure.io.xml.build;

import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bNoJumpInst.DecreaseInst;
import logic.domain.instructions.basic.bNoJumpInst.IncreaseInst;
import logic.domain.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.domain.instructions.basic.bNoJumpInst.NeutralInst;
import logic.domain.instructions.data.ArgumentData;
import logic.domain.instructions.data.InstructionData;
import logic.domain.instructions.synthetic.sJumpInst.GoToLabelInst;
import logic.domain.instructions.synthetic.sJumpInst.JumpEqualConstantInst;
import logic.domain.instructions.synthetic.sJumpInst.JumpEqualVariableInst;
import logic.domain.instructions.synthetic.sJumpInst.JumpZeroInst;
import logic.domain.instructions.synthetic.sNoJumpInst.AssignmentInst;
import logic.domain.instructions.synthetic.sNoJumpInst.ConstantAssignmentInst;
import logic.domain.instructions.synthetic.sNoJumpInst.ZeroVariableInst;
import logic.infrastructure.io.xml.dto.RawInstructions;
import logic.domain.label.SLabel;
import logic.domain.program.SProgram;
import logic.domain.program.SProgramImpl;
import logic.domain.variable.SVars;

import java.util.List;
import java.util.Locale;
import java.util.Map;

import static logic.infrastructure.io.xml.build.BuildUtils.*;
        import static logic.infrastructure.io.xml.build.BuildUtils.buildTargetLabel;

public class XmlProgramBuilder {

    public SProgram build(String programName, List<RawInstructions> raw) {
        SProgramImpl program = new SProgramImpl(programName.trim());

        for (RawInstructions r : raw) {
            InstructionData instName = InstructionData.valueOf(r.name().trim().toUpperCase(Locale.ROOT));
            SLabel lineLabel = buildLineLabel(r.labelText());
            SVars var = buildVar(r.varText());
            Map<ArgumentData, String> args = buildArgs(r.args());

            SInstruction inst = switch (instName) {
                // ----- Basic -----
                case INCREASE -> new IncreaseInst(var,lineLabel);
                case DECREASE -> new DecreaseInst(var,lineLabel);
                case NEUTRAL-> new NeutralInst(var,lineLabel);
                case JUMP_NOT_ZERO -> {
                    SLabel target = buildTargetLabel(args);
                    yield new JumpNotZeroInst(var, target, lineLabel);
                }
                // ----- Synthetic -----
                case ZERO_VARIABLE -> new ZeroVariableInst(var, lineLabel);
                case ASSIGNMENT -> {
                    String srcText = getRequiredArg(args, ArgumentData.ASSIGNED_VARIABLE);
                    SVars src = buildVar(srcText);
                    yield new AssignmentInst(var, src, lineLabel);
                }
                case CONSTANT_ASSIGNMENT -> {
                    long k = parseConstant(args);
                    yield new ConstantAssignmentInst(var, k, lineLabel);
                }
                case JUMP_ZERO -> {
                    SLabel target = buildTargetLabel(args);
                    yield new JumpZeroInst(var, target, lineLabel);
                }
                case JUMP_EQUAL_CONSTANT -> {
                    long k = parseConstant(args);
                    SLabel target = buildTargetLabel(args);
                    yield new JumpEqualConstantInst(var,  target,k, lineLabel);
                }
                case JUMP_EQUAL_VARIABLE -> {
                    String otherval = getRequiredArg(args, ArgumentData.VARIABLE_NAME);
                    SVars other = buildVar(otherval);
                    SLabel target = buildTargetLabel(args);
                    yield new JumpEqualVariableInst(var, other, target, lineLabel);
                }
                case GOTO_LABEL -> {
                    SLabel target = buildTargetLabel(args);
                    yield new GoToLabelInst(var,target, lineLabel);
                }
            };

            program.addInstruction(inst);
        }

        return program;
    }
}

