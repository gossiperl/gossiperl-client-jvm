package com.gossiperl.client;

import com.gossiperl.client.config.OverlayConfiguration;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Properties;

public class Supervisor {

    private HashMap<String, OverlayWorker> connections;
    private Logger log;

    public Supervisor(String log4jProperties) {
        this.connections = new HashMap<String, OverlayWorker>();
        Properties props = new Properties();
        if (log4jProperties == null) {
            try {
                props.load(getClass().getResourceAsStream("/log4j.properties"));
                PropertyConfigurator.configure(props);
            } catch (IOException ex) {
                System.out.println( "There was an error while loading log4j.properties from the JAR." );
                ex.printStackTrace();
                System.exit(100);
            }
        } else {
            String log4j_config = new File( log4jProperties ).getAbsolutePath();
            PropertyConfigurator.configureAndWatch(log4jProperties, 10 * 1000);
        }
        this.log = Logger.getLogger( Supervisor.class );
    }

    public Supervisor() {
        this(null);
    }

    public void connect(OverlayConfiguration config) throws GossiperlClientException, NoSuchAlgorithmException, UnsupportedEncodingException {
        if (this.connections.containsKey(config.getOverlayName())) {
            this.log.error("Client for " + config.getOverlayName() + " already present.");
            throw new GossiperlClientException("Client for " + config.getOverlayName() + " already present.");
        }
        OverlayWorker worker = new OverlayWorker(this, config);
        this.connections.put( config.getOverlayName(), worker );
        worker.start();
    }

    public void connect(String overlayName, int overlayPort, int clientPort, String clientName, String clientSecret, String symmetricKey)
            throws GossiperlClientException, NoSuchAlgorithmException, UnsupportedEncodingException {
        OverlayConfiguration cfg = new OverlayConfiguration();
        cfg.setOverlayName(overlayName);
        cfg.setOverlayPort(overlayPort);
        cfg.setClientPort(clientPort);
        cfg.setClientName(clientName);
        cfg.setClientSecret(clientSecret);
        cfg.setSymmetricKey(symmetricKey);
        this.connect( cfg );
    }

    public void disconnect(String overlayName) throws GossiperlClientException {
        if (this.connections.containsKey(overlayName)) {
            this.connections.get(overlayName).stop();
            this.connections.remove(overlayName);
        } else {
            this.log.error("[supervisor] No overlay connection: " + overlayName);
            throw new GossiperlClientException("[supervisor] No overlay connection: " + overlayName);
        }
    }

    public String[] subscribe(String overlayName, String[] events) throws GossiperlClientException {
        if (this.connections.containsKey(overlayName)) {
            return this.connections.get(overlayName).getState().subscribe( events );
        } else {
            this.log.error("[supervisor] No overlay connection: " + overlayName);
            throw new GossiperlClientException("[supervisor] No overlay connection: " + overlayName);
        }
    }

    public String[] unsubscribe(String overlayName, String[] events) throws GossiperlClientException {
        if (this.connections.containsKey(overlayName)) {
            return this.connections.get(overlayName).getState().unsubscribe(events);
        } else {
            this.log.error("[supervisor] No overlay connection: " + overlayName);
            throw new GossiperlClientException("[supervisor] No overlay connection: " + overlayName);
        }
    }

    public State.Status getCurrentState(String overlayName) throws GossiperlClientException {
        if (this.connections.containsKey(overlayName)) {
            return this.connections.get(overlayName).getState().getCurrentState();
        } else {
            this.log.error("[supervisor] No overlay connection: " + overlayName);
            throw new GossiperlClientException("[supervisor] No overlay connection: " + overlayName);
        }
    }

    public String[] getSubscriptions(String overlayName) throws GossiperlClientException {
        if (this.connections.containsKey(overlayName)) {
            return this.connections.get(overlayName).getState().getSubscriptions();
        } else {
            this.log.error("[supervisor] No overlay connection: " + overlayName);
            throw new GossiperlClientException("[supervisor] No overlay connection: " + overlayName);
        }
    }

    public void stop() {
        // TODO: implement
    }

    public static void main(String[] args) throws Exception {
        OverlayConfiguration config = new OverlayConfiguration();
        config.setClientName("jvm-client");
        config.setClientPort(54321);
        config.setClientSecret("jvm-client-secret");
        config.setSymmetricKey("v3JElaRswYgxOt4b");
        config.setOverlayName("gossiper_overlay_remote");
        config.setOverlayPort(6666);
        Supervisor sup = new Supervisor();
        sup.connect( config );
    }

}
