package design;

public class AnimationManager {
    private static boolean animationsEnabled=true;

    public static boolean isAnimationsEnabled(){
        return animationsEnabled;
    }

    public static void setAnimationsEnabled(boolean enabled){
        animationsEnabled=enabled;
    }
}
