package uiDisplay.design;

import java.util.HashMap;
import java.util.Map;
import javafx.scene.Scene;

public class SkinManager {

    private static final String BASE = "/skins/base.css";

    private static final Map<String, String> SKINS = new HashMap<>() {{
        put("Default", "/skins/default-skin.css");
        put("Dark Mode", "/skins/darkMode-skin.css");
        put("Sunset", "/skins/sunset-skin.css");
    }};


    public static void apply(Scene scene, String skinName) {
        scene.getStylesheets().clear();
        scene.getStylesheets().add(SkinManager.class.getResource("/skins/base.css").toExternalForm());

        String skinPath = SKINS.getOrDefault(skinName, SKINS.get("Default"));
        scene.getStylesheets().add(SkinManager.class.getResource(skinPath).toExternalForm());
    }

    public static String[] getAvailableSkins() {
        return SKINS.keySet().toArray(new String[0]);
    }
}
