package tk.junkmail.jetty;

import org.eclipse.jetty.websocket.servlet.ServletUpgradeRequest;
import org.eclipse.jetty.websocket.servlet.ServletUpgradeResponse;
import org.eclipse.jetty.websocket.servlet.WebSocketCreator;
import tk.junkmail.websockets.JunkmailWebsocketHandler;

import java.net.HttpCookie;
import java.util.List;
import java.util.UUID;

/**
 * @author Alexey Ponkin
 * @version 1, 24 Aug 2014
 */
public class JunkmailWebsocketCreator implements WebSocketCreator {

    private final JunkmailWebsocketHandler handler;

    public static final long COOKIE_MAX_EXPIRE = 60 * 60 * 24;

    public JunkmailWebsocketCreator(JunkmailWebsocketHandler handler){
        this.handler = handler;
    }

    @Override
    public Object createWebSocket(ServletUpgradeRequest servletUpgradeRequest, ServletUpgradeResponse servletUpgradeResponse) {
        String id = "";
        List<HttpCookie> cookies = servletUpgradeRequest.getCookies();
        if(cookies != null && !cookies.isEmpty()){
            id = cookies.get(0).getValue();
        } else {
            id = UUID.randomUUID().toString();
            HttpCookie cookie = new HttpCookie("id", id);
            cookie.setMaxAge(COOKIE_MAX_EXPIRE);
            cookie.setPath("/message");
            servletUpgradeResponse.setHeader("Set-Cookie", cookie.toString());
        }
        return new JunkmailWebSocketAdapter(id, handler);
    }
}
