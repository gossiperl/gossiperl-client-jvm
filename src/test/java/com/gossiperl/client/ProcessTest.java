package com.gossiperl.client;

import com.gossiperl.client.config.SupervisorConfiguration;
import junit.framework.TestCase;

public class ProcessTest extends TestCase {
    public void testProcess() throws Exception {
        Supervisor supervisor = new Supervisor();
        supervisor.connect(new SupervisorConfiguration());
        assertTrue( supervisor.getCurrentState("non_existing").equals( State.Status.DISCONNECTED ) );
    }
}
