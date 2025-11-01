package servlets.architecture.executeArch;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.domain.architecture.ArchitectureGen;
import logic.domain.program.SProgram;
import logic.engineFacade.api.EngineFacade;
import logic.system.data.architecture.ArchitectureManager;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.user.engine.EngineFacadeManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/checkArchitectureCompatibility")
public class CheckArchitectureCompatibilityServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String user = ServletUserUtils.getUsernameFromCookies(req);
        if (user == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(user);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No selected program."));
            return;
        }

        EngineFacade engine = EngineFacadeManager.getEngine(user, progName);
        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No engine loaded for user."));
            return;
        }

        String selectedArch = ArchitectureManager.getArchitecture(user, progName);
        if (selectedArch == null || selectedArch.isBlank()) {
            JsonObject resp = new JsonObject();
            resp.addProperty("state", "NO_ARCH");
            resp.addProperty("message", "No architecture selected.");
            ResponseWriter.write(res, resp);
            return;
        }

        int degree = DegreeManager.getDegree(user, progName);
        boolean supported = engine.isArchitectureCompatible(selectedArch, degree);

        if (!supported) {
            ResponseWriter.write(res, JsonResponseUtils.error(
                    "Program contains instructions not supported by architecture " + selectedArch + "."));
        } else {
            ResponseWriter.write(res, JsonResponseUtils.success("All instructions supported."));
        }
    }
}
