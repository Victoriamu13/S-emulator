package display;

import logic.engineFacade.facade.EngineFacade;

public final class EngineHolder {
    private EngineFacade engine;

    public boolean hasEngine() { return engine != null; }
    public EngineFacade get()  { return engine; }
    public void set(EngineFacade engine) { this.engine = engine; }
}
