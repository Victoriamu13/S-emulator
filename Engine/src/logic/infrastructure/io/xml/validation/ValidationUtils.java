package logic.infrastructure.io.xml.validation;

import logic.domain.instructions.data.InstructionData;
import logic.infrastructure.io.xml.dto.RawInstructions;

import java.util.Locale;

final class ValidationUtils {

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

     static String msg (RawInstructions r, String text){
        return "Instruction #" + r.line() + ": " + text;
    }
}
