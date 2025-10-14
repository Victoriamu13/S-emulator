package engineHolder;

import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.RunHistory;

public final class EngineHolder {
    EngineFacade engine;
    private final RunHistory history = new RunHistory();

    public boolean hasEngine() { return engine != null; }

    public static boolean hasEngine(EngineHolder holder) {
        return holder != null && holder.hasEngine();
    }

    public EngineFacade getEngine() { return engine; }

    public void set(EngineFacade eng) {
        this.engine=eng;
        if (eng != null) {
            eng.resetExpansionCache();
        }
        history.clear();
    }

    public RunHistory history() { return history; }
}
