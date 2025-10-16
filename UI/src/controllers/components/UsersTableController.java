package controllers.components;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import logic.domain.user.UserInfo;

import java.io.Closeable;
import java.util.List;
import java.util.Timer;

public class UsersTableController implements Closeable {
    private Timer timer;
    private UsersListRefresher listRefresher;
    private final BooleanProperty autoUpdate;
    private final IntegerProperty totalUsers;

    @FXML private TableView<UserInfo> usersTable;
    @FXML private TableColumn<UserInfo, String> colUserName;
    @FXML private TableColumn<UserInfo, Number> colPrograms;
    @FXML private TableColumn<UserInfo, Number> colFunctions;
    @FXML private TableColumn<UserInfo, Number> colCurrentCredits;
    @FXML private TableColumn<UserInfo, Number> colUsedCredits;
    @FXML private TableColumn<UserInfo, Number> colExecutions;
    @FXML private Label usersCountLabel;

    public UsersTableController(){
        this.autoUpdate=new SimpleBooleanProperty(true);
        this.totalUsers=new SimpleIntegerProperty(0);
    }

    @FXML
    public void initialize(){
        usersCountLabel.textProperty().bind(Bindings.concat("Active Users: (", totalUsers.asString(),")"));

        colUserName.setCellValueFactory(c->new SimpleStringProperty(c.getValue().username()));
        colPrograms.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().programsUploaded()));
        colFunctions.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().funcsAdded()));
        colCurrentCredits.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().currCredits()));
        colUsedCredits.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().creditsUsed()));
        colExecutions.setCellValueFactory(c -> new javafx.beans.property.SimpleIntegerProperty(c.getValue().totalExecutions()));

        startListRefresher();
    }

    public void startListRefresher(){
        listRefresher = new UsersListRefresher(autoUpdate,this::updateUsersTable);
        timer=new Timer(true);
        timer.schedule(listRefresher,0,3000);
    }


    private void updateUsersTable(List<UserInfo> users){
        Platform.runLater(()->{
            ObservableList<UserInfo> items = usersTable.getItems();
            items.clear();
            items.addAll(users);
            totalUsers.set(users.size());
        });
    }

    @Override
    public void close(){
        usersTable.getItems().clear();
        totalUsers.set(0);
        if(listRefresher!=null) listRefresher.cancel();
        if(timer!=null) timer.cancel();
    }


}
