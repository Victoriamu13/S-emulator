package logic.infrastructure.io.xml.load;

import logic.infrastructure.io.app.CurrentAppState;

import java.io.InputStream;
import java.nio.file.Path;

public class LoadService {
    private final XmlProgramLoader loader=new XmlProgramLoader();

    public LoadResult loadFromXml(InputStream inputStream, CurrentAppState state){
        LoadResult res = loader.load(inputStream);
        if(res.success() && res.program() != null){
            state.setProgram(res.program()); //overrun current program if program loaded successfully
        }
        return res;
    }
}
