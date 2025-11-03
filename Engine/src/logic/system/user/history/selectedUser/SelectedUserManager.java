package logic.system.user.history.selectedUser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SelectedUserManager {

    private static final Map<String,String> selectedUsers=new ConcurrentHashMap<>();

    public static void setSelectedUser(String requester,String selected){
        if(requester==null || selected==null ) return;;
        selectedUsers.put(requester,selected);
    }

    public static String getSelectedUser(String requester){
        if(requester==null) return null;;
        return selectedUsers.get(requester);
    }

    public static void clearSelection(String requester){
        if(requester==null) return;;
        selectedUsers.remove(requester);
    }
}
