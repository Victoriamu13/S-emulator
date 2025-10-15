package logic.domain.user;

import java.util.concurrent.ConcurrentHashMap;

public class CreditManager {

    private static final ConcurrentHashMap<String,Integer> userCredits = new ConcurrentHashMap<>();

    public static  synchronized void initializeUser(String username) {
        userCredits.putIfAbsent(username, 0);
    }

    public static synchronized void addCredits(String username, int amount) {
        userCredits.put(username, userCredits.getOrDefault(username, 0) + amount);
    }

    public static synchronized int getCredits(String username) {
        return userCredits.getOrDefault(username, 0);
    }

    public static synchronized boolean chargeCredits(String username, int amount) {
        int currentCredits = userCredits.getOrDefault(username, 0);
        if (currentCredits < amount) return false;

        userCredits.put(username, currentCredits - amount);
        return true;
    }

}
