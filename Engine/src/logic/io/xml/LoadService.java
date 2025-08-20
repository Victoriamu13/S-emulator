package logic.io.xml;

import logic.io.app.AppState;

import java.nio.file.Path;

public class LoadService {
    private final XmlProgramLoader loader=new XmlProgramLoader();

    public LoadResult loadFromXml(Path xmlPath, AppState state){
        LoadResult res = loader.load(xmlPath);
        if(res.success){
            state.setProgram(res.program); //overrun current program if program loaded successfully
        }
        return res;
    }
}
