package logic.infrastructure.io.xml.parser.composition;

import java.util.List;

public record CompositionParseResult(List<ComposeArgument> args, List<String> errors) {

    // ==== case success ====
    public boolean isOk(){return errors==null || errors.isEmpty();}

    public static CompositionParseResult success(List<ComposeArgument> args){
        return new CompositionParseResult(args,List.of());
    }

    // ==== case fail ====
    public static CompositionParseResult fail(List<String> errors) {
        return new CompositionParseResult(List.of(), errors);
    }

    public static CompositionParseResult fail(String error) {
        return fail(List.of(error));
    }
}
