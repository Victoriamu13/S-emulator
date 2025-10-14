package controllers.utils.client;

import controllers.utils.cookies.CookieManager;
import okhttp3.OkHttpClient;

public class HttpClientProvider {

    private static final CookieManager cookieManager=new CookieManager();
    private static final OkHttpClient client=new OkHttpClient.Builder()
            .cookieJar(cookieManager)
            .build();

    public static OkHttpClient getClient(){
        return client;
    }

    public static CookieManager getCookieManager(){
        return cookieManager;
    }
}
