package controllers.components.dashboardScreen;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import controllers.utils.refreshers.GenericRefresher;
import controllers.utils.refreshers.TimerManager;
import controllers.utils.server.ServerRequestUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import java.lang.reflect.Type;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import logic.system.user.info.UserInfo;
import okhttp3.FormBody;
import okhttp3.RequestBody;

import java.util.List;
import java.util.Timer;


public class UsersTableController{
    Timer timer;
    private GenericRefresher<UserInfo> refresher;
    private final BooleanProperty autoUpdate = new SimpleBooleanProperty(true);
    @FXML private TableView<UserInfo> usersTable;
    @FXML private TableColumn<UserInfo, String> colUserName;
    @FXML private TableColumn<UserInfo, Integer> colPrograms;
    @FXML private TableColumn<UserInfo, Integer> colFunctions;
    @FXML private TableColumn<UserInfo, Number> colCurrentCredits;
    @FXML private TableColumn<UserInfo, Number> colUsedCredits;
    @FXML private TableColumn<UserInfo, Integer> colExecutions;
    @FXML private Button btnUnselectUser;



    @FXML
    public void initialize(){
        setupColumns();
        usersTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                updateSelectedUser(newVal.username());
                highlightSelectedUser(newVal.username());
            }
        });

        startRefresher();
    }

    private void setupColumns(){
        colUserName.setCellValueFactory(c->new SimpleStringProperty(c.getValue().username()));
        colPrograms.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().programsUploaded()));
        colFunctions.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().funcsAdded()));
        colCurrentCredits.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().currCredits()));
        colUsedCredits.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().creditsUsed()));
        colExecutions.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().totalExecutions()));

    }

    private void updateSelectedUser(String selectedUser){
        RequestBody body=new FormBody.Builder()
                .add("user",selectedUser)
                .build();

        new Thread(()->{
        JsonElement response = ServerRequestUtils.sendPost("/selectedUser",body);
            if (response != null && response.isJsonObject()) {
                JsonObject obj = response.getAsJsonObject();
                if ("SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())) {
                    ServerRequestUtils.sendPost("/markHistoryUpdated",  RequestBody.create(new byte[0]));
                }
            }
        }).start();
    }

    private void highlightSelectedUser(String selectedUser) {
        usersTable.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(UserInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else if (item.username().equals(selectedUser)) {
                    setStyle("-fx-background-color: #e0f7fa;");
                } else {
                    setStyle("");
                }
            }
        });
    }

    public void startRefresher(){
        Type listType=new TypeToken<List<UserInfo>>(){}.getType();
        refresher = new GenericRefresher<>(
                autoUpdate,"/usersUpdated","/usersList",usersTable,listType);

        timer=new Timer(true);
        timer.schedule(refresher,0,1000);
    }

}
