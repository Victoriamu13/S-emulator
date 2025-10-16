package controllers.components;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import javafx.beans.property.BooleanProperty;
import logic.domain.user.UserInfo;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.lang.reflect.Type;
import java.util.List;
import java.util.TimerTask;
import java.util.function.Consumer;

public class UsersListRefresher extends TimerTask {
    private static final String SERVER_URL = "http://localhost:8080/usersList";
    private final OkHttpClient client;
    private final BooleanProperty autoUpdate;
    private final Consumer<List<UserInfo>> usersListConsumer;

    // ==== Constructor ===
    public UsersListRefresher(BooleanProperty autoUpdate,Consumer<List<UserInfo>> usersListConsumer) {
        this.autoUpdate = autoUpdate;
        this.usersListConsumer = usersListConsumer;
        this.client = new OkHttpClient();
    }

    @Override
    public void run(){
        if(!autoUpdate.get()) return;

        try {
            Request request = new Request.Builder().url(SERVER_URL).build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful() && response.body() != null) {

                    String json = response.body().string();
                    Type listType = new TypeToken<List<UserInfo>>() {
                    }.getType();
                    List<UserInfo> usersInfos = new Gson().fromJson(json, listType);
                    usersListConsumer.accept(usersInfos);

                }
            } }
            catch (Exception e) {}
    }
}
