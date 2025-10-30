package servlets.data.execution.normal;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;

import java.io.IOException;

@WebServlet("/initExecution")
public class InitExecutionServlet extends HttpServlet { //Only used when user starts execution not in ReRun mode
                                                        //Used by ExecutionScreenController

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Reset state before a new regular execution
        UpdateFlagsManager.markUpdated("initExecution");
        UpdateFlagsManager.markUpdated("inputs");

        JsonObject response = JsonResponseUtils.success("Execution state reset.");
        ResponseWriter.write(res, response);
    }
}