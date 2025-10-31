package servlets.data.expansionData.historyChain;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.data.expansion.HistoryChainManager;
import logic.engineFacade.api.EngineFacade;
import logic.engineFacade.model.InstructionDTO;
import logic.system.data.expansion.DegreeManager;
import logic.system.data.highlight.HighlightManager;
import logic.system.programs.functions.repository.FunctionRepository;
import logic.system.programs.repository.ProgramRepository;
import logic.system.programs.selectedProg.SelectedProgramManager;
import logic.system.updates.UpdateFlagsManager;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;
import java.util.List;

@WebServlet("/historyChain")

public class HistoryChainServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        String currentUser = ServletUserUtils.getUsernameFromCookies(req);
        if (currentUser == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }

        String progName = SelectedProgramManager.getSelectedProgram(currentUser);
        if (progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active program selection."));
            return;
        }

        String clear=req.getParameter("clear");
        if("true".equalsIgnoreCase(clear)){
            HistoryChainManager.clearHistoryChain(currentUser);
            UpdateFlagsManager.markUpdated("historyChain");

            ResponseWriter.write(res,JsonResponseUtils.success("History chain cleared."));
            return;
        }

        int index=0;
        int degree= DegreeManager.getDegree(currentUser,progName);
        try{
            index=Integer.parseInt(req.getParameter("index"));
        } catch (NumberFormatException ignored) {}


        String type = SelectedProgramManager.getSelectedType(currentUser);
        String name = SelectedProgramManager.getSelectedProgram(currentUser);
        EngineFacade engine = null;

        if ("program".equalsIgnoreCase(type)) {
            engine = ProgramRepository.getEngineForProgram(currentUser, name);
        } else if ("function".equalsIgnoreCase(type)) {
            engine = FunctionRepository.getEngineForFunction(currentUser, name);
        }

        if (engine == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Program engine not found."));
            return;
        }

        List<InstructionDTO> chain = engine.getExpansionHistoryChain(degree, index);
        HistoryChainManager.setHistoryChain(currentUser, chain);

        JsonObject response = JsonResponseUtils.success("Fetched history chain for selected degree.");
        response.add("chain", new Gson().toJsonTree(chain));

        String currentHighlight = HighlightManager.getHighlight(currentUser);
        response.addProperty("highlight", currentHighlight == null ? "" : currentHighlight);

        ResponseWriter.write(res, response);
    }

    @Override
    protected void doGet(HttpServletRequest req,HttpServletResponse res) throws IOException{
        String currentUser = ServletUserUtils.getUsernameFromCookies(req);
        if (currentUser == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("No active user session."));
            return;
        }
        List<InstructionDTO> chain = HistoryChainManager.getChain(currentUser);
        JsonObject response = JsonResponseUtils.success("Fetched history chain for user "+currentUser+".");
        response.add("chain", new Gson().toJsonTree(chain));

        String currentHighlight = HighlightManager.getHighlight(currentUser);
        response.addProperty("highlight", currentHighlight == null ? "" : currentHighlight);

        ResponseWriter.write(res, response);

    }
}
