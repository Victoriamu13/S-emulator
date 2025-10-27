package servlets.functions.selectedFunc;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.programs.functions.repository.FunctionRepository;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/selectedFunction")

public class SelectedFunctionServlet extends HttpServlet {
    //Update selected function for the logged-in user
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String function=req.getParameter("function"); //selected function that client wants to view
        String currentUser = ServletUserUtils.getUsernameFromCookies(req);

        if(function!=null && currentUser!=null){
            String internalName = FunctionRepository.getInternalName(function);
            SelectedProgramManager.setSelectedProgram(currentUser,"function",internalName); //set selected function for current logged-in user
            UpdateFlagsManager.markUpdated("degree");
            ResponseWriter.write(res, JsonResponseUtils.success("Function selected successfully."));
        }else{
            ResponseWriter.write(res,JsonResponseUtils.error("Missing user or function data."));
        }
    }

    //Fetch the currently selected program for the logged-in user
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String currentUser = ServletUserUtils.getUsernameFromCookies(req);
        if (currentUser == null) { //no logged-in user found
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String function = SelectedProgramManager.getSelectedProgram(currentUser); //get selected program for current logged-in user

        JsonObject response = JsonResponseUtils.success("Fetched selected function successfully.");
        response.addProperty("function", function);
        ResponseWriter.write(res, response);
    }

}
