package servlets.user.history.previousRun;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import servlets.utils.JsonResponseUtils;
import servlets.utils.ResponseWriter;
import servlets.utils.ServletUserUtils;

import java.io.IOException;

@WebServlet("/setReRunCookies")
public class SetReRunCookiesServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException {
        // Retrieve run details from client request (HistoryTable)
        String runID = req.getParameter("runID");
        String degree = req.getParameter("degree");
        String progName = req.getParameter("progName");
        String progType = req.getParameter("progType");

        if (runID == null || degree == null || progName == null) {
            ResponseWriter.write(res, JsonResponseUtils.error("Missing parameters for ReRun cookies."));
            return;
        }

        // Create cookies for run parameters → accessible by all servlets
        Cookie userCookie = new Cookie("username", ServletUserUtils.getUsernameFromCookies(req));
        Cookie runCookie = new Cookie("reRunID", runID);
        Cookie degreeCookie = new Cookie("reRunDegree", degree);
        Cookie progCookie = new Cookie("reRunProg", progName);
        Cookie typeCookie = new Cookie("reRunType", progType);

        for (Cookie c : new Cookie[]{userCookie,runCookie, degreeCookie, progCookie, typeCookie}) {
            c.setPath("/");   // make cookie accessible from all endpoints
            res.addCookie(c);
        }

        System.out.println("[Server] Created ReRun cookies: runID=" + runID +
                ", degree=" + degree + ", progName=" + progName);

        ResponseWriter.write(res, JsonResponseUtils.success("ReRun cookies created successfully."));
    }
}