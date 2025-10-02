package logic.infrastructure.io.xml.parser.composition;

import java.util.ArrayList;
import java.util.List;

import static logic.infrastructure.io.xml.validation.ValidationUtils.safeString;

public final class CompositionParser {

    private CompositionParser(){}

    // ==== Parses a top-level composition string into a list of ComposeArgument ====
    public static CompositionParseResult parseTopLevel(String str){
        str=safeString(str);
        if(str.isEmpty()){return CompositionParseResult.success(List.of());}

        // Split by top-level commas
        List<String> tokens = splitTopLevelByComma(str);
        if (tokens == null) {
            return CompositionParseResult.fail("Unbalanced parentheses in functionArguments.");
        }

        List<ComposeArgument> out = new ArrayList<>();
        for (String t : tokens) {
            String tok = t.trim();
            if (tok.isEmpty()) return CompositionParseResult.fail("Empty argument between commas.");
            if (tok.startsWith("(")) {
                FuncCallArgument call = parseCall(tok);
                if (call == null) return CompositionParseResult.fail("Invalid nested call: " + tok);
                out.add(call);
            } else {
                out.add(new VarArgument(tok));
            }
        }
        return CompositionParseResult.success(out);
    }


    // ==== Helper methods ====
    static List<String> splitTopLevelByComma(String s) {
        List<String> parts = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        int depth = 0;

        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') { depth++; cur.append(c); }
            else if (c == ')') { depth--; if (depth < 0) return null; cur.append(c); }
            else if (c == ',' && depth == 0) { parts.add(cur.toString()); cur.setLength(0); }
            else { cur.append(c); }
        }
        if (depth != 0) return null;  // safety check
        parts.add(cur.toString());
        return parts;
    }

    static FuncCallArgument parseCall(String s) {
        if (s.length() < 2 || s.charAt(0) != '(' || s.charAt(s.length()-1) != ')') return null;
        String inside = s.substring(1, s.length()-1).trim();

        List<String> top = splitTopLevelByComma(inside);
        if (top == null || top.isEmpty()) return null;

        String fnName = top.getFirst().trim();
        if (fnName.isEmpty()) return null;

        List<ComposeArgument> callArgs = new ArrayList<>();
        for (int i = 1; i < top.size(); i++) {
            String t = top.get(i).trim();
            if (t.isEmpty()) return null;
            if (t.startsWith("(")) {
                FuncCallArgument nested = parseCall(t);
                if (nested == null) return null;
                callArgs.add(nested);
            } else {
                callArgs.add(new VarArgument(t));
            }
        }
        return new FuncCallArgument(fnName, callArgs);
    }
}
