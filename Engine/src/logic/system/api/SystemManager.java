package logic.system.api;

import logic.engineFacade.api.EngineFacade;
import logic.system.user.info.UserInfo;

import java.util.List;

public interface SystemManager {

    // ==== USER MANAGEMENT ====
    boolean addUser(String username);
    boolean userExists(String username);
    void removeUser(String username);

    // ==== CREDIT MANAGEMENT ====
    void addCredits(String username, int amount);
    boolean chargeCredits(String username, int amount);
    int getCredits(String username);
    int getUsedCredits(String username);

    // ==== PROGRAMS ====
    void addProgram(String username, EngineFacade engine);

    // ==== USER INFO ====
    List<UserInfo> getAllUserInfo();
}
