package com.gossiperl.client;

import com.gossiperl.client.encryption.Aes256;
import com.gossiperl.client.serialization.DeserializeResult;
import com.gossiperl.client.serialization.DeserializeResultOK;
import com.gossiperl.client.serialization.Serializer;
import com.gossiperl.client.thrift.Digest;
import junit.framework.TestCase;
import org.apache.thrift.TBase;

import java.io.FileOutputStream;
import java.util.UUID;

public class ThriftTest extends TestCase {

    private Digest digest;
    private String encKey;

    public void setUp() {
        this.digest = new Digest();
        this.digest.setId(UUID.randomUUID().toString());
        this.digest.setName("test-serialize-client");
        this.digest.setPort(54321);
        this.digest.setHeartbeat( Util.getTimestamp() );
        this.digest.setSecret("test-serialize-secret");
        this.encKey = "v3JElaRswYgxOt4b";
    }

    public void tearDown() {
    }

    public void testSerializeDeserialize() throws Exception {
        Serializer serializer = new Serializer();
        byte[] envelope = serializer.serialize( this.digest );

        //FileOutputStream out1 = new FileOutputStream("/Users/rad/dev/my/gossiperl-client-jvm/unencrypted-thrift-data.thrift");
        //out1.write(envelope);
        //out1.close();

        DeserializeResult deserializedResult = serializer.deserialize( envelope );
        assertEquals( DeserializeResultOK.class, deserializedResult.getClass() );
        DeserializeResultOK resultOk = (DeserializeResultOK)deserializedResult;
        TBase deserializedDigest = resultOk.getDigest();
        assertEquals( Digest.class, deserializedDigest.getClass() );
        Digest resultDigest = (Digest)deserializedDigest;
        assertEquals( resultDigest.getName(), this.digest.getName() );
    }

    public void testSerializeDeserializeWithEncryption() throws Exception {
        // get the encryption:
        Aes256 aes = new Aes256(this.encKey);
        // serialize:
        Serializer serializer = new Serializer();
        byte[] envelope = serializer.serialize( this.digest );
        // encrypt:
        byte[] encryptedEnvelope = aes.encrypt(envelope);

        //FileOutputStream out2 = new FileOutputStream("/Users/rad/dev/my/gossiperl-client-jvm/encrypted-thrift-data.thrift");
        //out2.write(encryptedEnvelope);
        //out2.close();

        // decrypt:
        byte[] decryptedEnvelope = aes.decrypt(encryptedEnvelope);
        // deserialize:
        DeserializeResult deserializedResult = serializer.deserialize( decryptedEnvelope );
        assertEquals( DeserializeResultOK.class, deserializedResult.getClass() );
        // get the result digest:
        DeserializeResultOK resultOk = (DeserializeResultOK)deserializedResult;
        TBase deserializedDigest = resultOk.getDigest();
        assertEquals( Digest.class, deserializedDigest.getClass() );
        // check if data is in there...
        Digest resultDigest = (Digest)deserializedDigest;
        assertEquals( resultDigest.getName(), this.digest.getName() );
    }
}
