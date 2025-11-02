package logic.system.user.credits;

import java.util.concurrent.ConcurrentHashMap;

public class CreditManager {

    private static final ConcurrentHashMap<String,Integer> userCredits = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String,Integer> usedCredits = new ConcurrentHashMap<>();


    public static  synchronized void initializeUser(String username) {
        userCredits.putIfAbsent(username, 0);
        usedCredits.putIfAbsent(username,0);
    }

    public static synchronized void addCredits(String username, int amount) {
        userCredits.put(username, userCredits.getOrDefault(username, 0) + amount);
    }

    public static synchronized int getCredits(String username) {
        return userCredits.getOrDefault(username, 0);
    }

    public static synchronized  int getUsedCredits(String username) {
        return usedCredits.getOrDefault(username, 0);
    }

    public static synchronized boolean chargeCredits(String username, int amount) {
        int currentCredits = userCredits.getOrDefault(username, 0);
        if (currentCredits < amount) return false;

        userCredits.put(username, currentCredits - amount);
        usedCredits.put(username, usedCredits.getOrDefault(username,0)+amount);
        return true;
    }

    public static boolean consumeCredit(String user,long amount){
        if(amount<=0) return true;
        int current=getCredits(user);

        if(current<amount){
            userCredits.put(user,0);
            usedCredits.put(user, getUsedCredits(user) + current);
            return false;
        }else{
            userCredits.put(user, current - (int) amount);
            usedCredits.put(user, getUsedCredits(user) + (int) amount);
            return true;
        }
    }

}
