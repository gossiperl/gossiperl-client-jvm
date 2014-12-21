package com.gossiperl.client;

import com.gossiperl.client.config.OverlayConfiguration;
import com.gossiperl.client.listener.DefaultGossiperlClientListener;
import com.gossiperl.client.listener.GossiperlClientListener;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

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
            String log4jConfigPath = new File( log4jProperties ).getAbsolutePath();
            PropertyConfigurator.configureAndWatch(log4jConfigPath, 10 * 1000);
        }
        this.log = Logger.getLogger( Supervisor.class );
    }

    public Supervisor() {
        this(null);
    }

    public void connect(OverlayConfiguration config, GossiperlClientListener listener) throws GossiperlClientException, NoSuchAlgorithmException, UnsupportedEncodingException {
        if (isConnection(config.getOverlayName())) {
            this.log.error("Client for " + config.getOverlayName() + " already present.");
            throw new GossiperlClientException("Client for " + config.getOverlayName() + " already present.");
        }
        OverlayWorker worker = new OverlayWorker(this, config, listener);
        this.connections.put( config.getOverlayName(), worker );
        worker.start();
    }

    public void connect(OverlayConfiguration config) throws GossiperlClientException, NoSuchAlgorithmException, UnsupportedEncodingException {
        this.connect(config, new DefaultGossiperlClientListener());
    }

    public void disconnect(String overlayName) throws GossiperlClientException {
        if (isConnection(overlayName)) {
            this.connections.get(overlayName).stop();
        } else {
            this.log.error("[supervisor] No overlay connection: " + overlayName);
            throw new GossiperlClientException("[supervisor] No overlay connection: " + overlayName);
        }
    }

    protected void disconnected(OverlayConfiguration config) {
        this.connections.remove(config.getOverlayName());
    }

    public List<String> subscribe(String overlayName, List<String> events) throws GossiperlClientException {
        if (isConnection(overlayName)) {
            return this.connections.get(overlayName).getState().subscribe( events );
        } else {
            this.log.error("[supervisor] No overlay connection: " + overlayName);
            throw new GossiperlClientException("[supervisor] No overlay connection: " + overlayName);
        }
    }

    public List<String> unsubscribe(String overlayName, List<String> events) throws GossiperlClientException {
        if (isConnection(overlayName)) {
            return this.connections.get(overlayName).getState().unsubscribe(events);
        } else {
            this.log.error("[supervisor] No overlay connection: " + overlayName);
            throw new GossiperlClientException("[supervisor] No overlay connection: " + overlayName);
        }
    }

    public State.Status getCurrentState(String overlayName) throws GossiperlClientException {
        if (isConnection(overlayName)) {
            return this.connections.get(overlayName).getState().getCurrentState();
        } else {
            this.log.error("[supervisor] No overlay connection: " + overlayName);
            throw new GossiperlClientException("[supervisor] No overlay connection: " + overlayName);
        }
    }

    public List<String> getSubscriptions(String overlayName) throws GossiperlClientException {
        if (isConnection(overlayName)) {
            return this.connections.get(overlayName).getState().getSubscriptions();
        } else {
            this.log.error("[supervisor] No overlay connection: " + overlayName);
            throw new GossiperlClientException("[supervisor] No overlay connection: " + overlayName);
        }
    }

    public int getNumberOfConnections() {
        return this.connections.size();
    }

    public boolean isConnection(String overlayName) {
        return this.connections.containsKey( overlayName );
    }

    public void stop() {
        for ( OverlayWorker worker : this.connections.values() ) {
            worker.stop();
        }
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
