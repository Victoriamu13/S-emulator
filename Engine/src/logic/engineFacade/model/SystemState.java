package logic.engineFacade.model;

import java.io.Serializable;

public class SystemState implements Serializable {  //for loading and saving to files executed programs //bonus
    public final String loadedXmlPath;
    public final RunHistory history;

    public SystemState(String loadedXmlPath, RunHistory history) {
        this.loadedXmlPath = loadedXmlPath;
        this.history = history;
    }
}
