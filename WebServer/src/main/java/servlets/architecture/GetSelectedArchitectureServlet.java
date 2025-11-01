package servlets.architecture;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.data.architecture.ArchitectureManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/getSelectedArchitecture")
public class GetSelectedArchitectureServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String user = ServletUserUtils.getUsernameFromCookies(req);
        if (user == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(user);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No program selected."));
            return;
        }

        String selectedArch = ArchitectureManager.getArchitecture(user, progName);

        JsonObject response = JsonResponseUtils.success("Architecture retrieved.");
        response.addProperty("selected", selectedArch);

        ResponseWriter.write(res, response);
    }
}
