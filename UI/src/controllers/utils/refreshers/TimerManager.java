package controllers.utils.refreshers;

import java.util.*;

import static controllers.screens.ScreenManager.EXECUTION;

public class TimerManager {
    private static final Map<String, List<Timer>> timersByScreen = new HashMap<>();
    private static String currentScreen = null;

    // Register a timer for a specific screen
    public static synchronized void register(String screenId, Timer timer) {
        if (timer == null) return;
        timersByScreen.computeIfAbsent(screenId, k -> new ArrayList<>()).add(timer);
    }

    // Stop all timers for the given screen
    public static synchronized void stop(String screenId) {
        List<Timer> timers = timersByScreen.remove(screenId);
        if (timers != null) {
            for (Timer t : timers) t.cancel();
            System.out.println("[TimerManager] Stopped all timers for screen: " + screenId);
        }
    }
    // Stop previous timers, keep new ones
    public static synchronized void switchScreen(String newScreenId) {
        if (currentScreen != null && !currentScreen.equals(newScreenId)) {
            if (EXECUTION.equals(currentScreen)) {
                stop(currentScreen);
            } else {
                System.out.println("[TimerManager] Keeping timers alive for screen: " + currentScreen);
            }
        }

        currentScreen = newScreenId;
    }
    // Stop all timers globally
    public static synchronized void stopAll() {
        for (String screenId : timersByScreen.keySet()) {
            stop(screenId);
        }
        timersByScreen.clear();
        currentScreen = null;
    }
}
