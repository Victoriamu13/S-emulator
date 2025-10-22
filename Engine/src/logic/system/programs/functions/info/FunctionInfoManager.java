package logic.system.programs.functions.info;

import logic.system.programs.functions.repository.FunctionEntry;
import logic.system.programs.functions.repository.GlobalFunctionRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FunctionInfoManager {

    public static List<FunctionInfo> getAllFunctionsInfo(){
        List<FunctionInfo> infos=new ArrayList<>();

        for(FunctionEntry funcEntry : GlobalFunctionRepository.allFunctions()){
            infos.add(new FunctionInfo(
                    funcEntry.userString(),
                    funcEntry.progName(),
                    funcEntry.uploader(),
                    funcEntry.instCount(),
                    funcEntry.maxDegree()
            ));
        }
        return infos;
    }
}
