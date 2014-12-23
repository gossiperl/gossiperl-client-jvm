package com.gossiperl.client;

import com.gossiperl.client.exceptions.GossiperlUnsupportedSerializableTypeException;
import com.gossiperl.client.serialization.*;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class SerializationTest extends TestCase {

    private Serializer serializer;
    private String digestType;
    private List<CustomDigestField> digestInfo;

    public void setUp() {
        this.serializer = new Serializer();
        this.digestType = "someDigestType";
        this.digestInfo = new ArrayList<CustomDigestField>();
        try {
            this.digestInfo.add(new CustomDigestField("some_property", "this is some string value to test", "string", (short)1));
            this.digestInfo.add(new CustomDigestField("some_other_property", new Long(1234), "i32", (short)2));
        } catch (GossiperlUnsupportedSerializableTypeException ex) {
            System.out.println("[Serialization test]: Error in set up.");
            ex.printStackTrace();
        }
    }

    public void tearDown() {
    }

    public void testSerializeDeserialize() throws Exception {
        byte[] envelope = this.serializer.serializeArbitrary( this.digestType, this.digestInfo );
        DeserializeResult result = this.serializer.deserializeArbitrary( this.digestType, envelope, this.digestInfo );
        assertTrue(result.getClass().getName().equals(DeserializeResultCustomOK.class.getName()));
        DeserializeResultCustomOK finalResult = (DeserializeResultCustomOK)result;
        assertEquals(this.digestType, finalResult.getDigestType());
        assertTrue( finalResult.getResultData().containsKey("some_property") );
        assertTrue( finalResult.getResultData().containsKey("some_other_property") );
    }

}
