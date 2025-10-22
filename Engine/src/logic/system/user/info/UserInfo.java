package logic.system.user.info;

public record UserInfo(String username, int programsUploaded, int funcsAdded,
                       int currCredits, int creditsUsed, int totalExecutions) { }
