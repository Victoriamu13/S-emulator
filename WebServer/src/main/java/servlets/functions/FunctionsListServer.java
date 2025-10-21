package servlets.functions;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.programs.functions.info.FunctionInfo;
import logic.system.programs.functions.info.FunctionInfoManager;
import servlets.utils.ResponseWriter;

import java.io.IOException;
import java.util.List;

@WebServlet("/functionsList")

public class FunctionsListServer extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
         List<FunctionInfo> allFunctionsInfo = FunctionInfoManager.getAllFunctionsInfo();
        ResponseWriter.write(res,allFunctionsInfo);
    }
}
