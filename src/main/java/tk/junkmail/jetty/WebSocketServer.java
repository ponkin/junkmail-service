package tk.junkmail.jetty;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import tk.junkmail.websockets.JunkmailWebsocketHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Alexey Ponkin
 * @version 1, 24 Aug 2014
 */
public class WebSocketServer {

    private final Server server;

    private static final Logger LOG = LoggerFactory.getLogger(WebSocketServer.class);

    public WebSocketServer(int port, JunkmailWebsocketHandler handler){
        server = new Server();
        ServerConnector connector = new ServerConnector(server);
        connector.setPort(port);
        server.addConnector(connector);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        ServletHolder holderEvents = new ServletHolder("ws-events", new JunkmailServlet(handler));
        context.addServlet(holderEvents, "/messages/*");
    }

    public void start() throws Exception {
        server.start();
    }

    public void stop() throws Exception {
        server.stop();
    }
}
