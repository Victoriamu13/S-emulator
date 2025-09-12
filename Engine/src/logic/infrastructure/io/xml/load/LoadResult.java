package logic.infrastructure.io.xml.load;

import logic.domain.program.SProgram;

import java.util.Collections;
import java.util.List;

public class LoadResult {
    public final boolean success;
    public final SProgram program;
    public final List<String> errors;

    public LoadResult(boolean success, SProgram program, List<String> errors) {
        this.success = success;
        this.program = program;
        this.errors = errors;
    }

    public static LoadResult success(SProgram p) {
        return new LoadResult(true, p, Collections.emptyList());
    }

    public static LoadResult failed(List<String> errs) {
        return new LoadResult(false, null, errs);
    }
}
