package logic.system.api;

public class SelectedClientState {
    private static String currentUser;
    private static String selectedProgram;
    private static String selectedArchitecture;
    private static int currentCredits;
    private static String executionMode;

    // ========== USER ==========
    public static String getCurrentUser() {
        return currentUser;
    }

    public static void setCurrentUser(String user) {
        currentUser = user;
    }

    // ========== PROGRAM ==========
    public static String getSelectedProgram() {
        return selectedProgram;
    }

    public static void setSelectedProgram(String program) {
        selectedProgram = program;
    }

    // ========== ARCHITECTURE ==========
    public static String getSelectedArchitecture() {
        return selectedArchitecture;
    }

    public static void setSelectedArchitecture(String architecture) {
        selectedArchitecture = architecture;
    }

    // ========== CREDITS ==========
    public static int getCurrentCredits() {
        return currentCredits;
    }

    public static void setCurrentCredits(int credits) {
        currentCredits = credits;
    }

    // ========== EXECUTION MODE ==========
    public static String getExecutionMode() {
        return executionMode;
    }

    public static void setExecutionMode(String mode) {
        executionMode = mode;
    }

    // Utility to clear when logging out or switching users
    public static void reset() {
        currentUser = null;
        selectedProgram = null;
        selectedArchitecture = null;
        currentCredits = 0;
        executionMode = null;
    }
}
