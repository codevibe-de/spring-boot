package pizza;

import org.h2.tools.Server;
import org.springframework.stereotype.Component;
import org.springframework.core.NestedExceptionUtils;

import javax.annotation.PostConstruct;
import java.sql.SQLException;

@Component
public class H2Launcher implements Runnable {

    private Server server;

    @PostConstruct
    @Override
    public void run() {
        System.out.println("Launching H2 TCP Server");
        try {
            server = Server.createTcpServer("-tcp", "-ifNotExists", "-tcpPort", "9092").start();
        } catch (SQLException e) {
            var rootCause = NestedExceptionUtils.getRootCause(e);
            if (rootCause != null && rootCause.getMessage().equals("Address already in use: bind")) {
                System.out.println("Server seems to be running already (maybe in some other context?)");
            }
            else {
                throw new IllegalStateException("Failed to launch H2 TCP server", e);
            }
        }
    }

    public void stop() {
        if (this.server != null) {
            System.out.println("Stopping H2 TCP Server");
            this.server.stop();
        }
    }
}
