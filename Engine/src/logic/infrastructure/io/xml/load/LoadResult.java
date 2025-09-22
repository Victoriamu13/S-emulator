package logic.infrastructure.io.xml.load;

import logic.domain.program.SProgram;
import logic.domain.program.functions.FunctionRepository;

import java.util.Collections;
import java.util.List;

public record LoadResult(boolean success, SProgram program, FunctionRepository functions, List<String> errors) {

    public static LoadResult success(SProgram p, FunctionRepository repo) {
        return new LoadResult(true, p, repo, Collections.emptyList());
    }

    public static LoadResult failed(List<String> errs) {
        return new LoadResult(false, null, new FunctionRepository(), errs);
    }
}
