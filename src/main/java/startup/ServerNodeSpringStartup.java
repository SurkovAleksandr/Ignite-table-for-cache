package startup;

import org.apache.ignite.Ignite;
import org.apache.ignite.Ignition;

public class ServerNodeSpringStartup {
    /**
     * Start up node with specified configuration.
     * 
     * @param args Command line arguments, none required.
     * @throws Exception If failed.
     **/
    public static void main(String[] args) throws Exception {
        Ignite ignite = Ignition.start("Cluster-server.xml");
        ignite.cluster().active(true);
    }
}