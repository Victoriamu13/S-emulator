package servlets.user;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import logic.domain.user.UserInfo;
import logic.domain.user.UserInfoManager;
import servlets.utils.ResponseWriter;

import java.io.IOException;
import java.util.List;

@WebServlet("/usersList")

public class UsersListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        List<UserInfo> allUsersInfos= UserInfoManager.getAllUserInfo();
        ResponseWriter.write(res, allUsersInfos);
    }
}
