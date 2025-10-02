package logic.infrastructure.io.xml.build;

import logic.domain.instructions.SInstruction;
import logic.domain.instructions.basic.bJumpInst.JumpNotZeroInst;
import logic.domain.instructions.basic.bNoJumpInst.DecreaseInst;
import logic.domain.instructions.basic.bNoJumpInst.IncreaseInst;
import logic.domain.instructions.basic.bNoJumpInst.NeutralInst;
import logic.domain.instructions.data.ArgumentData;
import logic.domain.instructions.data.InstructionData;
import logic.domain.instructions.synthetic.sJumpInst.*;
import logic.domain.instructions.synthetic.sNoJumpInst.AssignmentInst;
import logic.domain.instructions.synthetic.sNoJumpInst.ConstantAssignmentInst;
import logic.domain.instructions.synthetic.sNoJumpInst.ZeroVariableInst;
import logic.domain.instructions.synthetic.sNoJumpInst.QuoteInst;
import logic.domain.label.SLabel;
import logic.domain.label.SLabelImpl;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import logic.domain.variable.SVarsImpl;
import logic.domain.variable.SVarsType;
import logic.infrastructure.io.xml.dto.RawInstructions;
import logic.infrastructure.io.xml.parser.composition.CompositionParseResult;
import logic.infrastructure.io.xml.parser.composition.CompositionParser;

import java.util.*;

public class BuildUtils {

    //build instruction from components
    public static SInstruction constructInstruction(
            InstructionData type,
            SVars var,
            SLabel label,
            Map<ArgumentData,String> args)
    {
        return switch(type) {
            case INCREASE -> new IncreaseInst(var, label);
            case DECREASE -> new DecreaseInst(var, label);
            case NEUTRAL -> new NeutralInst(var, label);
            case JUMP_NOT_ZERO -> {
                SLabel target = buildTargetLabel(args);
                yield new JumpNotZeroInst(var, target, label);
            }
            case ZERO_VARIABLE -> new ZeroVariableInst(var, label);
            case ASSIGNMENT -> {
                String srcText = getRequiredArg(args, ArgumentData.ASSIGNED_VARIABLE);
                SVars src = buildVar(srcText);
                yield new AssignmentInst(var, src, label);
            }
            case CONSTANT_ASSIGNMENT -> {
                long k = parseConstant(args);
                yield new ConstantAssignmentInst(var, k, label);
            }
            case JUMP_ZERO -> {
                SLabel target = buildTargetLabel(args);
                yield new JumpZeroInst(var, target, label);
            }
            case JUMP_EQUAL_CONSTANT -> {
                long k = parseConstant(args);
                SLabel target = buildTargetLabel(args);
                yield new JumpEqualConstantInst(var, target, k, label);
            }
            case JUMP_EQUAL_VARIABLE -> {
                String otherval = getRequiredArg(args, ArgumentData.VARIABLE_NAME);
                SVars other = buildVar(otherval);
                SLabel target = buildTargetLabel(args);
                yield new JumpEqualVariableInst(var, other, target, label);
            }
            case GOTO_LABEL -> {
                SLabel target = buildTargetLabel(args);
                yield new GoToLabelInst(var, target, label);
            }
            case QUOTE -> {
                String funcName = getRequiredArg(args, ArgumentData.FUNCTION_NAME);
                String rawArgs = args.getOrDefault(ArgumentData.FUNCTION_ARGUMENTS, "");
                CompositionParseResult parsed = CompositionParser.parseTopLevel(rawArgs);

                yield new QuoteInst(var, funcName, parsed.args(), label);
            }
            case JUMP_EQUAL_FUNCTION -> {
                SLabel target = buildTargetLabel(args);
                String funcName = getRequiredArg(args, ArgumentData.FUNCTION_NAME);
                String rawArgs  = args.getOrDefault(ArgumentData.FUNCTION_ARGUMENTS, "");
                CompositionParseResult parsed = CompositionParser.parseTopLevel(rawArgs);

                yield new JumpEqualFuncInst(var, funcName, parsed.args(), target,label);
            }
        };
    }

    // ==== Build from RawInstructions ====
    public static SInstruction buildInstruction(RawInstructions raw){

        InstructionData instName=InstructionData.valueOf(raw.name().trim().toUpperCase(Locale.ROOT));
        SLabel lineLabel=buildLineLabel(raw.labelText());
        SVars var=buildVar(raw.varText());
        Map<ArgumentData,String> args=buildArgs(raw.args());
        return constructInstruction(instName, var, lineLabel, args);
    }

    // ==== Build SVars from text ====
     public static SVars buildVar(String variableName) {
        String s = variableName.trim();
         if (s.equalsIgnoreCase("y")) return SVars.RESULT;

         char head = Character.toLowerCase(s.charAt(0));   // x / z
        int n = Integer.parseInt(s.substring(1));
        return (head == 'x')
                ? new SVarsImpl(SVarsType.INPUT, n)
                : new SVarsImpl(SVarsType.WORK, n);
    }

    // ==== Build SLabel from text ====
     public static SLabel buildLineLabel(String labelName) {
        String s = (labelName == null ? "" : labelName.trim());
        if (s.isEmpty()) return SpecialLabels.EMPTY;
        if (s.equalsIgnoreCase("EXIT")) return SpecialLabels.EXIT;
        int num = Integer.parseInt(s.substring(1));
        return new SLabelImpl(num);
    }

    // ==== Build args map from raw map ====
     static Map<ArgumentData, String> buildArgs(Map<String, String> raw) {
        if (raw == null || raw.isEmpty()) return Map.of();
        EnumMap<ArgumentData, String> out = new EnumMap<>(ArgumentData.class);
        for (Map.Entry<String,String> e : raw.entrySet()) {
            ArgumentData key = ArgumentData.fromString(e.getKey());
            String val = e.getValue().trim();

            switch (key) {
                case JNZ_LABEL, GOTO_LABEL, JZ_LABEL, JE_CONSTANT_LABEL, JE_VARIABLE_LABEL ->
                    out.put(key, normalizeLabelValue(val));   // EXIT / L..
                case ASSIGNED_VARIABLE, VARIABLE_NAME -> out.put(key, normalizeVarText(val));      // y / xN / zN
                default -> out.put(key, val);
            }
        }
        return out;
    }

    // ==== Helpers functions ====
    private static String normalizeLabelValue(String v) {
        return v.equalsIgnoreCase("EXIT") ? "EXIT" : v.toUpperCase(Locale.ROOT);
    }

    private static String normalizeVarText(String v) {
        if (v.equalsIgnoreCase("y")) return "y";
        return Character.toLowerCase(v.charAt(0)) + v.substring(1); // x5 / z10
    }

    public static SLabel buildTargetLabel(Map<ArgumentData, String> args) {
        String target = firstNonNull(
                args.get(ArgumentData.JNZ_LABEL),
                args.get(ArgumentData.GOTO_LABEL),
                args.get(ArgumentData.JZ_LABEL),
                args.get(ArgumentData.JE_CONSTANT_LABEL),
                args.get(ArgumentData.JE_VARIABLE_LABEL),
                args.get(ArgumentData.JE_FUNCTION_LABEL)
        );

        if (target.equals("EXIT")) return SpecialLabels.EXIT;
        int num = Integer.parseInt(target.substring(1));
        return new SLabelImpl(num);
    }

    @SafeVarargs
    private static <T> T firstNonNull(T... values) {
        for (T v : values) if (v != null) return v;
        return null;
    }

    public static long parseConstant(Map<ArgumentData, String> args) {
        String s = getRequiredArg(args, ArgumentData.CONSTANT_VALUE);
        return Long.parseLong(s.trim());
    }

   public static String getRequiredArg(Map<ArgumentData, String> args, ArgumentData key) {
        return args.get(key);
    }
}
