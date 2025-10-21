package logic.system.programs.info;

import logic.system.programs.repository.ProgramEntry;
import logic.system.programs.repository.ProgramRepository;

import java.util.ArrayList;
import java.util.List;

public class ProgramInfoManager {

    public static List<ProgramsInfo> getAllProgramsInfo() {
       List<ProgramsInfo> infos=new ArrayList<>();

       for(ProgramEntry progEntry : ProgramRepository.allPrograms()){
           infos.add(new ProgramsInfo(
                   progEntry.getProgName(),
                     progEntry.uploadedBy(),
                     progEntry.getNumberInstructions(),
                     progEntry.getMaxExpansionDegree(),
                     progEntry.getRunCount(),
                     progEntry.getAvgCreditCost()
           ));
       }
       return infos;
    }

}
