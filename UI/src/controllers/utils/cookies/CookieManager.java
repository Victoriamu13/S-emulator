package controllers.utils.cookies;

import okhttp3.CookieJar;
import okhttp3.Cookie;
import okhttp3.HttpUrl;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CookieManager implements CookieJar {

    private final Map<String,Map<String,Cookie>> cookieStore=new HashMap<>();

    @NotNull
    @Override
    public List<Cookie> loadForRequest(@NotNull HttpUrl url){
        String host=url.host();

        synchronized (this){
            if(cookieStore.containsKey(host)){
                return new ArrayList<>(cookieStore.get(host).values());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public void saveFromResponse(@NotNull HttpUrl url, @NotNull List<Cookie> cookies){
        String host=url.host();

        synchronized (this){
            Map<String,Cookie> hostCookies = cookieStore.computeIfAbsent(host,k->new HashMap<>());

            for(Cookie cookie : cookies){
                hostCookies.put(cookie.name(),cookie);
            }
        }
    }

    public synchronized Map<String,Map<String,Cookie>> getAllCookies(){
        return new HashMap<>(cookieStore);
    }

    public synchronized String getCookieValue(String host,String cookieName){
        if(cookieStore.containsKey(host)){
            Cookie cookie=cookieStore.get(host).get(cookieName);
            if(cookie!=null){
                return cookie.value();
            }
        }
        return null;
    }

    public synchronized void clearAllCookies(){
        cookieStore.clear();;
        System.out.println("[CookieManager] Cleared all cookies.");
    }

}
