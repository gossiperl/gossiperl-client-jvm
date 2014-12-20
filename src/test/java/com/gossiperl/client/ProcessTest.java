package com.gossiperl.client;

import com.gossiperl.client.config.OverlayConfiguration;
import junit.framework.TestCase;

import java.util.UUID;

public class ProcessTest extends TestCase {

    private Supervisor supervisor;
    private OverlayConfiguration config;
    private String[] subscriptions;

    public void setUp() {
        this.supervisor = new Supervisor();
        this.config = new OverlayConfiguration();
        this.config.setClientName("jvm-client");
        this.config.setClientPort(54321);
        this.config.setClientSecret("jvm-client-secret");
        this.config.setSymmetricKey("v3JElaRswYgxOt4b");
        this.config.setOverlayName("gossiper_overlay_remote");
        this.config.setOverlayPort(6666);
        this.subscriptions = new String[2];
        this.subscriptions[0] = "member_in";
        this.subscriptions[1] = "digestForwardableTest";
    }

    public void tearDown() {
    }

    public void testProcess() throws Exception {
        this.supervisor.connect( this.config );
        assertTrue( this.supervisor.getCurrentState( this.config.getOverlayName() ).equals( State.Status.DISCONNECTED ) );
        // TODO: implement
    }

    public void testNonExistingOverlay() throws Exception {
        boolean thrown = false;
        try {
            supervisor.getCurrentState("non_existing");
        } catch (GossiperlClientException ex) {
            thrown = true;
        }
        assertTrue( thrown );
    }
}
