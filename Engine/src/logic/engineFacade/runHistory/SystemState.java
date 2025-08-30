package logic.engineFacade.runHistory;

import java.io.Serializable;

public class SystemState implements Serializable {  //for loading and saving to files executed programs //bonus
    private static final long serialVersionUID = 1L;

    public final String loadedXmlPath;
    public final RunHistory history;

    public SystemState(String loadedXmlPath, RunHistory history) {
        this.loadedXmlPath = loadedXmlPath;
        this.history = history;
    }
}
