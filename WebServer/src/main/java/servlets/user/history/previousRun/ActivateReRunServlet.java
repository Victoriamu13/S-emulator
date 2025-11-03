package servlets.user.history.previousRun;

import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.RunRecord;
import logic.system.data.execution.runHistory.ReRunStateManager;
import logic.system.data.expansion.DegreeManager;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.user.engine.EngineFacadeManager;
import logic.system.user.history.userHstory.UserHistoryManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.util.stream.Collectors;

@WebServlet("/activateReRun")
public class ActivateReRunServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String username = ServletUserUtils.getUsernameFromCookies(req);
        if (username == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String progName = req.getParameter("progName");
        String progType = req.getParameter("progType");
        String degreeStr = req.getParameter("degree");
        String runIdStr = req.getParameter("runID");
        String ownerUser = req.getParameter("ownerUser");

        System.out.println("========== [ActivateReRunServlet] ==========");
        System.out.println("[ReRun] username = " + username);
        System.out.println("[ReRun] ownerUser = " + ownerUser);
        System.out.println("[ReRun] progName = " + progName);
        System.out.println("[ReRun] progType = " + progType);
        System.out.println("[ReRun] degreeStr = " + degreeStr);
        System.out.println("[ReRun] runIdStr = " + runIdStr);
        System.out.println("===========================================");

        if (progName == null || progType == null || degreeStr == null || runIdStr == null || ownerUser == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Missing parameters for ReRun activation."));
            return;
        }

        int degree, runId;;
        try {
            degree = Integer.parseInt(degreeStr);
            runId = Integer.parseInt(runIdStr);
        } catch (NumberFormatException e) {
            System.out.println("[ReRun][ERROR] Failed parsing degree/runID → degreeStr=" + degreeStr + ", runIdStr=" + runIdStr);
            ResponseWriter.write(res, JsonResponseUtils.error("Invalid degree parameter."));
            return;
        }

        ReRunStateManager.setReRun(username, true);
        DegreeManager.setDegree(username,progName, degree);
        SelectedProgramManager.setSelectedProgram(username, progType, progName);

        EngineFacade ownerEngine  = EngineFacadeManager.getEngine(ownerUser, progName);
        if (ownerEngine == null) {
            System.out.println("[ReRun][ERROR] No engine found for ownerUser=" + ownerUser + ", prog=" + progName);

            ResponseWriter.write(res, JsonResponseUtils.error("Original engine not found for user " + ownerUser));
            return;
        }

        //Register original owners engine also under current user
        EngineFacadeManager.registerEngine(username, progName, ownerEngine);
        System.out.println("[ReRun] Engine cloned/registered for user=" + username);

        // Fetch run info
        RunRecord record = UserHistoryManager.getRunRecord(ownerUser, runId);
        if (record == null) {
            System.out.println("[ReRun][ERROR] No RunRecord found for ownerUser=" + ownerUser + ", runId=" + runId);

            ResponseWriter.write(res, JsonResponseUtils.error("Run record not found for original user."));
            return;
        }

        List<String> inputStrings = Arrays.stream(record.inputs()).mapToObj(String::valueOf).collect(Collectors.toList());
        ownerEngine.prepareInputsFields(record.degree(), inputStrings);

        System.out.println("[ReRun] Inputs loaded: " + inputStrings);
        System.out.println("[ReRun] Degree: " + record.degree());
        System.out.println("[ReRun] ReRun mode successfully initialized for " + username);

        JsonObject response = JsonResponseUtils.success(
                "ReRun activated successfully using original engine of user " + ownerUser
        );
        ResponseWriter.write(res, response);
    }

}
