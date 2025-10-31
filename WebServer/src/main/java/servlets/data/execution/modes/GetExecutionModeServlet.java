package servlets.data.execution.modes;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.data.execution.execMode.ExecutionModeManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/getExecutionMode")
public class GetExecutionModeServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String currentUser= ServletUserUtils.getUsernameFromCookies(req);

        if(currentUser==null){
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String mode = ExecutionModeManager.getMode(currentUser);
        if (mode == null) mode = "NORMAL";

        JsonObject response = JsonResponseUtils.success("Mode fetched.");
        response.addProperty("mode", mode);
        ResponseWriter.write(res, response);
    }
}
