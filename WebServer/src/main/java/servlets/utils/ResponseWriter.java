package servlets.utils;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class ResponseWriter {
    private static final Gson gson=new Gson();

    public static void write(HttpServletResponse res, JsonObject json) throws IOException{
        res.setContentType("application/json");
        res.getWriter().write(gson.toJson(json));
    }
}
