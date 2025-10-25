package servlets.programs.selectedProg;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;

import java.io.IOException;

@WebServlet("/selectedProgram")

public class SelectedProgramServlet extends HttpServlet {

    //Update selected program for the logged-in user
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String program=req.getParameter("program"); //selected program that client wants to view
        String currentUser=null;

        //get current logged-in user from cookies
        if(req.getCookies()!=null){
            for(Cookie c: req.getCookies()){
                if("username".equals(c.getName())){
                    currentUser=c.getValue();
                    break;
                }
            }
        }

        if(program!=null && currentUser!=null){
            SelectedProgramManager.setSelectedProgram(currentUser,"program",program); //set selected program for current logged-in user
            UpdateFlagsManager.markUpdated("degree");
            ResponseWriter.write(res, JsonResponseUtils.success("Program selected successfully."));
        }else{
            ResponseWriter.write(res,JsonResponseUtils.error("Missing user or program data."));
        }
    }

    //Fetch the currently selected program for the logged-in user
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String currentUser = null; //current logged-in user

        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) { //find current logged-in user from cookies
                if ("username".equals(c.getName())) {
                    currentUser = c.getValue();
                    break;
                }
            }
        }

        if (currentUser == null) { //no logged-in user found
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String program = SelectedProgramManager.getSelectedProgram(currentUser); //get selected program for current logged-in user

        JsonObject response = JsonResponseUtils.success("Fetched selected program successfully.");
        response.addProperty("program", program);
        ResponseWriter.write(res, response);
    }
}
