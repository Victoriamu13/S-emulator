package controllers.components.load;

import com.google.gson.JsonObject;
import controllers.utils.server.ServerRequestUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.stage.FileChooser;
import okhttp3.*;

import java.io.File;


public class HeaderController {
    @FXML private Button btnLoadFile;
    @FXML private TextField filePathField;
    @FXML private TextField userNameField;
    @FXML private Button btnChargeCredits;
    @FXML private TextField creditsField;
    @FXML private TextField creditsInputField;

    @FXML
    private void initialize(){
        btnLoadFile.setOnAction(e->onLoadFile());
        btnChargeCredits.setOnAction(e->onChargeCredits());

        JsonObject obj = ServerRequestUtils.sendGet("/currentUser");

        if (obj.has("state")) {
            String state = obj.get("state").getAsString();

            if ("SUCCESS".equals(state)) {
                String username = obj.has("username") ? obj.get("username").getAsString() : "Unknown";
                userNameField.setText(username);
            }
        } else {
            userNameField.setText("Unknown");
        }
    }


    private void onLoadFile(){
        FileChooser fc=new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("XML files","*.xml"));
        File f=fc.showOpenDialog(btnLoadFile.getScene().getWindow());
        if(f==null) return;

        RequestBody body=new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file",f.getName(), RequestBody.create(f, MediaType.parse("application/xml")))
                .addFormDataPart("user",userNameField.getText())
                .build();

        JsonObject obj = ServerRequestUtils.sendPost("/loadProgram", body);

       if(obj!= null && "SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())){
           filePathField.setText(f.getAbsolutePath());
       }
    }


    private void onChargeCredits(){
        String user=userNameField.getText();
        String amountCredits=creditsInputField.getText();

        RequestBody body=new FormBody.Builder()
                .add("user",user)
                .add("credits",amountCredits)
                .build();

        JsonObject obj = ServerRequestUtils.sendPost("/chargeCredits", body);

      if(obj!=null && "SUCCESS".equalsIgnoreCase(obj.get("state").getAsString())){
          String newCredits=obj.has("credits") ? obj.get("credits").getAsString() : "";
          creditsField.setText(newCredits);
      }
    }


}
