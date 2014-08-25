package tk.junkmail.jetty;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import tk.junkmail.websockets.JunkmailWebsocketHandler;

/**
 * @author Alexey Ponkin
 * @version 1, 24 Aug 2014
 */
public class JunkmailServlet extends WebSocketServlet {

    public static final int IDLE_TIMEOUT = 60000;

    private final JunkmailWebsocketCreator creator;

    public JunkmailServlet(JunkmailWebsocketHandler handler){
        creator = new JunkmailWebsocketCreator(handler);
    }

    @Override
    public void configure(WebSocketServletFactory webSocketServletFactory) {
        webSocketServletFactory.getPolicy().setIdleTimeout(IDLE_TIMEOUT);
        webSocketServletFactory.setCreator(creator);
    }
}
