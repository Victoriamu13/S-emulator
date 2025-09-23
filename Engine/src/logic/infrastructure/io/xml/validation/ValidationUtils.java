package logic.infrastructure.io.xml.validation;

import logic.domain.instructions.data.InstructionData;
import logic.infrastructure.io.xml.dto.RawInstructions;
import logic.infrastructure.io.xml.parser.composition.CompositionParseResult;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class ValidationUtils {

    private ValidationUtils(){}

     static boolean isBlank (String s){
        return s == null || s.trim().isEmpty();
    }

     static String makeValidString (String s){
        return s == null ? "" : s.trim();
    }

     static boolean equalIgnoreCase (String a, String b){
        return a != null && a.equalsIgnoreCase(b);
    }

    //True if variable is y OR xN OR zN (case-insensitive, N>=1).
     static boolean isValidVariable (String variable){
        if (isBlank(variable)) return false;

        String s = variable.trim();
        if (s.equalsIgnoreCase("y")) return true;

        char c = s.charAt(0);
        if (c == 'x' || c == 'X' || c == 'z' || c == 'Z') {
            String num = s.substring(1);
            return num.matches("[1-9][0-9]*");
        }
        return false;
    }

    static boolean isValidInputVariable(String token) {
        String s = safeString(token);
        if (s.length() < 2) return false;
        char h = s.charAt(0);
        if (h != 'x' && h != 'X') return false;
        String tail = s.substring(1);
        return tail.matches("[1-9][0-9]*");
    }

    // True if "EXIT" or in range L1..L99 (case-insensitive).
     static boolean isLabelInRange (String label){
        if (isBlank(label)) return false;

        String s = label.trim();
        if (s.equalsIgnoreCase("EXIT")) return true;

        return label.matches("(?i)L([1-9]|[1-9][0-9])");
    }

    static InstructionData tryInstruction (String name){
        if (isBlank(name)) return null;
        try {
            return InstructionData.valueOf(name.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public static int parseXIndex(String token) {
        String s = safeString(token);
        return Integer.parseInt(s.substring(1));
    }


    public static String getArgIgnoreCase(java.util.Map<String,String> map, String key) {
        for (var e : map.entrySet()) {
            if (e.getKey() != null && e.getKey().equalsIgnoreCase(key)) {
                return safeString(e.getValue());
            }
        }
        return "";
    }

    public static String safeString(String s) { return (s == null) ? "" : s.trim(); }

    public static <T> List<T> safeList(List<T> list){return (list==null) ? List.of() : list;}

    public static String msg (RawInstructions r, String text){
        return "Instruction #" + r.line() + ": " + text;
    }

    public static int countParenGroups(String s) {
        s = safeString(s);
        if (s.isEmpty()) return 0;

        int depth = 0;
        int count = 0;
        StringBuilder token = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);

            if (c == '(') {
                depth++;
                token.append(c);
            } else if (c == ')') {
                depth--;
                if (depth < 0) {
                    return -1;
                }
                token.append(c);
            } else if (c == ',' && depth == 0) {
                if (!token.toString().trim().isEmpty()) count++;
                token.setLength(0);
            } else {
                token.append(c);
            }
        }
        if (depth != 0) return -1;

        if (!token.toString().trim().isEmpty()) count++;

        return count;
    }

    public static void addPrefixed(List<String> src, String prefix, List<String> dst) {
        for (String m : src) dst.add(prefix + m);
    }

    public static Map<String,String> safeArgs(Map<String,String> m) {
        return (m == null) ? Map.of() : m;
    }

    public static void addParseErrors(List<String> errors, RawInstructions r, CompositionParseResult parsed) {
        for (String e : parsed.errors()) {
            errors.add(msg(r, e));
        }
    }
}
