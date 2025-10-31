package servlets.data.execution.debug;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.data.highlight.HighlightInstructionManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/currentInstruction")
public class CurrentInstructionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String user = ServletUserUtils.getUsernameFromCookies(req);
        if (user == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String program = SelectedProgramManager.getSelectedProgram(user);
        if (program == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No selected program."));
            return;
        }

        int index = HighlightInstructionManager.getCurrentInstruction(user, program);

        JsonObject response = JsonResponseUtils.success("Fetched current instruction index.");
        response.addProperty("index", index);
        ResponseWriter.write(res, response);
    }
}
