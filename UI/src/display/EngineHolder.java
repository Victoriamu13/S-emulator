package display;

import logic.engineFacade.facade.EngineFacade;
import logic.engineFacade.runHistory.RunHistory;

public final class EngineHolder {
    private EngineFacade engine;
    private final RunHistory history = new RunHistory();

    public boolean hasEngine() { return engine != null; }
    public EngineFacade get()  { return engine; }

    public void set(EngineFacade engine) {
        this.engine = engine;
        history.clear();
    }

    public RunHistory history() { return history; }
}
