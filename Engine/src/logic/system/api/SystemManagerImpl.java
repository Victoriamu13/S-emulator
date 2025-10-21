package logic.system.api;

import logic.domain.instructions.SInstruction;
import logic.domain.program.functions.FunctionLookup;
import logic.engineFacade.api.EngineFacade;
import logic.system.programs.functions.repository.FunctionEntry;
import logic.system.programs.functions.repository.GlobalFunctionRepository;
import logic.system.programs.repository.ProgramRepository;
import logic.system.user.CreditManager;
import logic.system.user.UserInfo;
import logic.system.user.UserInfoManager;
import logic.system.user.UserManager;

import java.util.List;
import java.util.Map;

public class SystemManagerImpl implements SystemManager{

    // ==== USERS ====
    @Override public boolean addUser(String username){
        return UserManager.addUser(username);
    }

    @Override public boolean userExists(String username){
        return UserManager.userExists(username);
    }

    @Override public void removeUser(String username){
        UserManager.removeUser(username);
    }

    // ===== CREDITS =====
    @Override public void addCredits(String username, int amount){
       CreditManager.addCredits(username, amount);
    }

    @Override public boolean chargeCredits(String username, int amount){
        return CreditManager.chargeCredits(username, amount);
    }

    @Override public int getCredits(String username){
        return CreditManager.getCredits(username);
    }

    @Override public int getUsedCredits(String username){
        return CreditManager.getUsedCredits(username);
    }

    // ==== PROGRAMS ====
    @Override public void addProgram(String username, EngineFacade engine){
        FunctionLookup lookup=engine.getProgram().getFunctionLookup();
        GlobalFunctionRepository.addFunctions(lookup,username, engine.getProgramName());
        ProgramRepository.addProgram(username,engine);
    }


    // ==== INFO ====
    @Override public List<UserInfo> getAllUserInfo(){
        return UserInfoManager.getAllUserInfo();
    }

}
