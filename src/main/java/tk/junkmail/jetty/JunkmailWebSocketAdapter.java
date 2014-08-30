package tk.junkmail.jetty;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import tk.junkmail.websockets.JunkmailWebsocketHandler;

/**
 * @author Alexey Ponkin
 * @version 1, 24 Aug 2014
 */
public class JunkmailWebSocketAdapter extends WebSocketAdapter {

    private final JunkmailWebsocketHandler handler;
    private final String id;

    public JunkmailWebSocketAdapter(String id, JunkmailWebsocketHandler handler){
        this.handler = handler;
        this.id = id;
    }

    @Override
    public void onWebSocketError(Throwable cause) {
        handler.onWebSocketError(id, cause);
    }

    @Override
    public void onWebSocketConnect(Session sess) {
        super.onWebSocketConnect(sess);
        handler.onWebSocketConnect(id, sess);
    }

    @Override
    public void onWebSocketClose(int statusCode, String reason) {
        super.onWebSocketClose(statusCode, reason);
        handler.onWebSocketClose(id, statusCode, reason);
    }

    @Override
    public void onWebSocketText(String message) {
        handler.onWebSocketText(id, message, getSession());
    }
}
