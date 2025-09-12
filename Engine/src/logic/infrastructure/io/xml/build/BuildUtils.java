package logic.infrastructure.io.xml.build;

import logic.domain.instructions.data.ArgumentData;
import logic.domain.label.SLabel;
import logic.domain.label.SLabelImpl;
import logic.domain.label.SpecialLabels;
import logic.domain.variable.SVars;
import logic.domain.variable.SVarsImpl;
import logic.domain.variable.SVarsType;

import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class BuildUtils {

     static SVars buildVar(String variableName) {
        String s = variableName.trim();
        if (s.equalsIgnoreCase("y")) {
            return SVars.RESULT;
        }
        char head = Character.toLowerCase(s.charAt(0));   // x / z
        int n = Integer.parseInt(s.substring(1));
        return (head == 'x')
                ? new SVarsImpl(SVarsType.INPUT, n)
                : new SVarsImpl(SVarsType.WORK, n);
    }

     static SLabel buildLineLabel(String labelName) {
        String s = (labelName == null ? "" : labelName.trim());
        if (s.isEmpty()) return SpecialLabels.EMPTY;
        if (s.equalsIgnoreCase("EXIT")) return SpecialLabels.EXIT;
        int num = Integer.parseInt(s.substring(1));
        return new SLabelImpl(num);
    }

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
                args.get(ArgumentData.JE_VARIABLE_LABEL)
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
