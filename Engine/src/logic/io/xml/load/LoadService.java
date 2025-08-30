package logic.io.xml.load;

import logic.io.app.CurrentAppState;

import java.nio.file.Path;

public class LoadService {
    private final XmlProgramLoader loader=new XmlProgramLoader();

    public LoadResult loadFromXml(Path xmlPath, CurrentAppState state){
        LoadResult res = loader.load(xmlPath);
        if(res.success && res.program != null){
            state.setProgram(res.program); //overrun current program if program loaded successfully
        }
        return res;
    }
}
