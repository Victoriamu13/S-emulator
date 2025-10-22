package servlets.user.info;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.system.api.SystemManager;
import logic.system.api.SystemManagerImpl;
import logic.system.user.info.UserInfo;
import servlets.utils.ResponseWriter;

import java.io.IOException;
import java.util.List;

@WebServlet("/usersList")

public class UsersListServlet extends HttpServlet {
    private final SystemManager systemManager=new SystemManagerImpl();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException{
        List<UserInfo> allUsersInfos= systemManager.getAllUserInfo();
        ResponseWriter.write(res, allUsersInfos);
    }
}
