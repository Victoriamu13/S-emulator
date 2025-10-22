package servlets.programs;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.programs.info.ProgramsInfo;
import logic.system.programs.info.ProgramInfoManager;
import servlets.utils.ResponseWriter;

import java.io.IOException;
import java.util.List;

@WebServlet("/programsList")

public class ProgramsListServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{

        List<ProgramsInfo> ProgramsInfo = ProgramInfoManager.getAllProgramsInfo();
        ResponseWriter.write(res, ProgramsInfo);
    }
}
