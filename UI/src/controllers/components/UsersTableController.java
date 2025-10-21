package controllers.components;

import com.google.gson.reflect.TypeToken;
import controllers.utils.refreshers.GenericRefresher;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import java.lang.reflect.Type;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.system.user.UserInfo;
import java.util.List;
import java.util.Timer;


public class UsersTableController{
    Timer timer;
    private GenericRefresher<UserInfo> refresher;
    private final BooleanProperty autoUpdate = new SimpleBooleanProperty(true);
    private final IntegerProperty totalUsers = new SimpleIntegerProperty(0);

    @FXML private TableView<UserInfo> usersTable;
    @FXML private TableColumn<UserInfo, String> colUserName;
    @FXML private TableColumn<UserInfo, Integer> colPrograms;
    @FXML private TableColumn<UserInfo, Integer> colFunctions;
    @FXML private TableColumn<UserInfo, Number> colCurrentCredits;
    @FXML private TableColumn<UserInfo, Number> colUsedCredits;
    @FXML private TableColumn<UserInfo, Integer> colExecutions;
    @FXML private Label usersCountLabel;



    @FXML
    public void initialize(){
        usersCountLabel.textProperty().bind(Bindings.concat("Active Users: (", totalUsers.asString(),")"));

        colUserName.setCellValueFactory(c->new SimpleStringProperty(c.getValue().username()));
        colPrograms.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().programsUploaded()));
        colFunctions.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().funcsAdded()));
        colCurrentCredits.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().currCredits()));
        colUsedCredits.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().creditsUsed()));
        colExecutions.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().totalExecutions()));

        startRefresher();
    }

    public void startRefresher(){
        Type listType=new TypeToken<List<UserInfo>>(){}.getType();
        refresher = new GenericRefresher<>(
                autoUpdate,"/usersUpdated","/usersList",usersTable,listType);

        timer=new Timer(true);
        timer.schedule(refresher,0,1000);
    }

}
