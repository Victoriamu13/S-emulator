package logic.engineFacade.api;

import logic.domain.architecture.ArchitectureGen;
import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.expandProgram.ProgramExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.instructions.info.InstructionInfo;
import logic.domain.program.SProgram;
import logic.domain.program.SProgramImpl;
import logic.domain.program.info.ExpandedProgramInfo;
import logic.domain.program.info.ProgramInfo;
import logic.domain.program.info.ProgramInfoImpl;
import logic.engineFacade.model.InstructionDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.copyOf;

public class EngineFacadeUtils {

    // ==== Validation helpers ====

    public static int validDegree(int degree, int maxDegree) {
        if (degree < 0) return 0;
        if (degree > maxDegree) return maxDegree;
        return degree;
    }

    public static InstructionDTO toDto(SProgram program, InstructionInfo ins) {
        String commandText = ins.getFullCommand();
        if (program != null && commandText != null) {
            commandText = findUserStringName(commandText, program);
        }
        return new InstructionDTO(
                ins.getIndex(),
                ins.isSynthetic() ? "S" : "B",
                ins.getVariableName(),
                ins.getLabelName(),
                commandText,
                cyclesTextOf(ins.getName(), ins.getCycles())
        );
    }

    // ==== Cycles text formatting for synthetic instructions ====

    public static String cyclesTextOf(String nameUpper, int numeric) {
        String n = (nameUpper == null ? "" : nameUpper.trim().toUpperCase(java.util.Locale.ROOT));
        return switch (n) {
            case "QUOTE" -> "x+5";
            case "JUMP_EQUAL_FUNCTION" -> "x+6";
            default -> Integer.toString(numeric);
        };
    }

    // ==== Expand program up to degree and return new SProgram ====

    public static  SProgram materializeProgram(SProgram program, int usedDegree) {
        if (usedDegree == 0) return program; // regular program - no expansion

        // expand program to degree
        ExpansionContext ctx = ExpansionContext.seedFrom(program);
        ProgramExpander exp = new ProgramExpander(ctx);
        List<SInstruction> expanded = exp.expandToDegree(program.getInstructions(), usedDegree);

        SProgramImpl expandedProg = new SProgramImpl(program.getName() + "_deg" + usedDegree);
        for (SInstruction ins : expanded) expandedProg.addInstruction(ins);
        expandedProg.setFunctionLookup(program.getFunctionLookup());
        return expandedProg;
    }

    // ==== ProgramInfo (basic / expanded) ====

    public static ProgramInfo getProgramInfo(SProgram program,int maxDegree,int degree) {
        int used = validDegree(degree,maxDegree);
        if (used == 0) {
            return new ProgramInfoImpl(program); // basic info
        }
        ExpansionContext ctx = ExpansionContext.seedFrom(program);
        ProgramExpander  exp = new ProgramExpander(ctx);
        return new ExpandedProgramInfo(program, used, exp, ctx); // expanded info
    }


    // ==== Comparator ====
    public static Comparator<String> numericAwareComparator() {
        return (a, b) -> {
                String prefixA = a.replaceAll("\\d", "");
                String prefixB = b.replaceAll("\\d", "");
                if (prefixA.equals(prefixB)) {
                    int numA = Integer.parseInt(a.replaceAll("\\D", ""));
                    int numB = Integer.parseInt(b.replaceAll("\\D", ""));
                    return Integer.compare(numA, numB);
                }
            return a.compareTo(b); //default
        };
    }


    // ==== Input helpers ====
    public static long[] normalizeInputsForProgram(long[] userInputs,  List<String> inputsUsed) {

        int requiredLen = 0;
        for (String s : inputsUsed) {
            s = s.trim();
            if (s.matches("x\\d+")) {
                int idx = Integer.parseInt(s.substring(1));
                if (idx > requiredLen) requiredLen = idx;
            }
        }
        return copyOf(userInputs, requiredLen);
    }

    public static int getRequiredInputsCount(List<String> inputs) {
        int required = 0;
        for (String var : inputs) {
            if (var.matches("x\\d+")) {
                int idx = Integer.parseInt(var.substring(1));
                if (idx > required) required = idx;
            }
        }
        return required;
    }

    public static long[] parseInputValues(List<String> values, int required, List<String> variableNames) {
        long[] out = new long[required];
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < required; i++) {
            String val = (i < values.size()) ? values.get(i).trim() : "";
            if (val.isEmpty()) {
                out[i] = 0; //default input is 0
            } else {
                try {
                    out[i] = Long.parseLong(val);
                } catch (NumberFormatException e) {
                    String varName = (variableNames != null && i < variableNames.size())
                            ? variableNames.get(i)
                            : "position " + (i + 1);
                    errors.add("Illegal value '" + val + "' for " + varName);
                }
            }
        }

        if (!errors.isEmpty()) throw new IllegalArgumentException(String.join("\n", errors));
        return out;
    }

    // ==== Replace raw internal names with user-strings names ====

    public static String findUserStringName(String rawCommand,SProgram program) {

        for (String fnName : program.getFunctionLookup().allFunctionNames()) {
            String userStr = program.getFunctionLookup().userStringOf(fnName);
            if (!fnName.equals(userStr)) {
                rawCommand = rawCommand.replaceAll("(?i)\\b" + fnName + "\\b", userStr);
            }
        }
        return rawCommand;
    }


    //Count supported instructions for a given architecture and degree.

    private int countSupported(SProgram program, ArchitectureGen gen, int degree,int maxDegree) {
        if (program == null || gen == null) return 0;

        var info = getProgramInfo(program, maxDegree, degree);
        int count = 0;
        for (var inst : info.getInstructions()) {
            if (gen.supports(inst)) count++;
        }
        return count;
    }


    //Count unsupported instructions for a given architecture and degree.
    private int countUnsupported(SProgram program, ArchitectureGen gen, int degree,int maxDegree) {
        if (program == null || gen == null) return 0;

        var info = getProgramInfo(program, maxDegree, degree);
        int count = 0;
        for (var inst : info.getInstructions()) {
            if (!gen.supports(inst)) count++;
        }
        return count;
    }


}
