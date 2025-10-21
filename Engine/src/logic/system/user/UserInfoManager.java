package logic.system.user;

import logic.system.programs.functions.repository.GlobalFunctionRepository;
import logic.system.programs.repository.ProgramRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserInfoManager {
    private static final Map<String,Integer> runsCount = new HashMap<>();

    public static synchronized void addExecution(String username){
        runsCount.put(username,runsCount.getOrDefault(username,0)+1);
    }

    public static List<UserInfo> getAllUserInfo(){
        List<UserInfo> infos=new ArrayList<>();

        for(String user : UserManager.getAllActiveUsers()) {
            int programsUploaded=(int) ProgramRepository.allPrograms().stream()
                    .filter(pe -> pe.uploadedBy().equals(user))
                    .count();

            int contributedFuncs= GlobalFunctionRepository.getFuncsContributedBy(user);
            int currentCredits= CreditManager.getCredits(user);
            int usedCredits= CreditManager.getUsedCredits(user);
            int totalExec=runsCount.getOrDefault(user,0);

            infos.add(new UserInfo(user, programsUploaded, contributedFuncs, currentCredits, usedCredits, totalExec));
        }
        return infos;
    }
}