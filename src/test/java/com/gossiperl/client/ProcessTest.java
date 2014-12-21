package com.gossiperl.client;

import com.gossiperl.client.config.OverlayConfiguration;
import com.gossiperl.client.exceptions.GossiperlClientException;
import junit.framework.TestCase;

import java.util.ArrayList;

public class ProcessTest extends TestCase {

    private Supervisor supervisor;
    private OverlayConfiguration config;
    private ArrayList<String> subscriptions;

    public void setUp() {
        this.supervisor = new Supervisor();
        this.config = new OverlayConfiguration();
        this.config.setClientName("jvm-client");
        this.config.setClientPort(54321);
        this.config.setClientSecret("jvm-client-secret");
        this.config.setSymmetricKey("v3JElaRswYgxOt4b");
        this.config.setOverlayName("gossiper_overlay_remote");
        this.config.setOverlayPort(6666);
        this.subscriptions = new ArrayList<String>();
        this.subscriptions.add( "member_in" );
        this.subscriptions.add( "digestForwardableTest" );
    }

    public void tearDown() {
    }

    public void testProcess() throws Exception {
        // connect
        this.supervisor.connect( this.config );
        Thread.sleep(3000);
        assertTrue( this.supervisor.getCurrentState( this.config.getOverlayName() ).equals( State.Status.CONNECTED ) );
        // duplicate overlay
        boolean duplicateErrorThrown = false;
        try {
            this.supervisor.connect(this.config);
        } catch (GossiperlClientException ex) {
            duplicateErrorThrown = true;
        }
        assertTrue( duplicateErrorThrown );
        // subscribe
        assertTrue(this.supervisor.subscribe(this.config.getOverlayName(), this.subscriptions).equals( this.subscriptions ));
        Thread.sleep(3000);
        // unsubscribe
        assertTrue(this.supervisor.unsubscribe(this.config.getOverlayName(), this.subscriptions).equals(new ArrayList<String>()));
        Thread.sleep(3000);
        // disconnect:
        supervisor.disconnect(this.config.getOverlayName());
        Thread.sleep(1500);
        assertEquals( this.supervisor.getNumberOfConnections(), 0 );
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
