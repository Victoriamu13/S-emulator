package logic.engineFacade.model;

import java.util.List;

public record LoadOutcome(boolean success, List<String> errors) {

    public static LoadOutcome ok() {return new LoadOutcome(true, List.of());}
    public static LoadOutcome fail(List<String> errs){return new LoadOutcome(false,errs);}
}
