package engineHolder;

import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.RunHistory;

public final class EngineHolder {
    private EngineFacade engine;
    private final RunHistory history = new RunHistory();

    public boolean hasEngine() { return engine != null; }
    public EngineFacade getEngine()  { return engine; }

    public void set(EngineFacade engine) {
        this.engine = engine;
        history.clear();
    }

    public RunHistory history() { return history; }
}
