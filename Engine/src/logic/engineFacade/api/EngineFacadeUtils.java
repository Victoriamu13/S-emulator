package logic.engineFacade.api;

import logic.domain.expand.expandProgram.ExpansionContext;
import logic.domain.expand.expandProgram.ProgramExpander;
import logic.domain.instructions.SInstruction;
import logic.domain.program.SProgram;
import logic.domain.program.SProgramImpl;
import logic.domain.program.info.ExpandedProgramInfo;
import logic.domain.program.info.ProgramInfo;
import logic.domain.program.info.ProgramInfoImpl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class EngineFacadeUtils {


    public static int validDegree(int degree, int maxDegree) {
        if (degree < 0) return 0;
        if (degree > maxDegree) return maxDegree;
        return degree;
    }

    public static  SProgram materializeProgram(SProgram program, int usedDegree) {
        if (usedDegree == 0) return program; // regular program - no expansion

        // expand program to degree
        ExpansionContext ctx = ExpansionContext.seedFrom(program);
        ProgramExpander exp = new ProgramExpander(ctx);
        List<SInstruction> expanded = exp.expandToDegree(program.getInstructions(), usedDegree);

        SProgramImpl expandedProg = new SProgramImpl(program.getName() + "_deg" + usedDegree);
        for (SInstruction ins : expanded) {
            expandedProg.addInstruction(ins);
        }
        expandedProg.setFunctionLookup(program.getFunctionLookup());
        return expandedProg;
    }

    public static ProgramInfo getProgramInfo(SProgram program,int maxDegree,int degree) {
        int used = validDegree(degree,maxDegree);
        if (used == 0) {
            return new ProgramInfoImpl(program);
        }
        ExpansionContext ctx = ExpansionContext.seedFrom(program);
        ProgramExpander  exp = new ProgramExpander(ctx);
        return new ExpandedProgramInfo(program, used, exp, ctx);
    }


    public static Comparator<String> numericAwareComparator() {
        return (a, b) -> {
            try {
                String prefixA = a.replaceAll("\\d", "");
                String prefixB = b.replaceAll("\\d", "");
                if (prefixA.equals(prefixB)) {
                    int numA = Integer.parseInt(a.replaceAll("\\D", ""));
                    int numB = Integer.parseInt(b.replaceAll("\\D", ""));
                    return Integer.compare(numA, numB);
                }
            } catch (Exception ignored) {}
            return a.compareTo(b);
        };
    }


    public static long[] normalizeInputsForProgram(long[] userInputs,  List<String> inputsUsed) {

        int requiredLen = 0;
        for (String s : inputsUsed) {
            s = s.trim();
            if (s.matches("x\\d+")) {
                int idx = Integer.parseInt(s.substring(1));
                if (idx > requiredLen) {
                    requiredLen = idx;
                }
            }
        }
        return java.util.Arrays.copyOf(userInputs, requiredLen);
    }

    public static int getRequiredInputsCount(List<String> inputs) {
        List<String> inputsUsed = inputs;
        int required = 0;
        for (String var : inputsUsed) {
            if (var.matches("x\\d+")) {
                int idx = Integer.parseInt(var.substring(1));
                if (idx > required) {
                    required = idx;
                }
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
                out[i] = 0;
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

        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\n", errors));
        }
        return out;
    }


}
