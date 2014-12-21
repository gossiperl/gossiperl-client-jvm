package com.gossiperl.client.transport;

import com.gossiperl.client.GossiperlClientException;
import com.gossiperl.client.OverlayWorker;
import com.gossiperl.client.encryption.Aes256;
import com.gossiperl.client.serialization.CustomDigestField;
import com.gossiperl.client.serialization.DeserializeResult;
import com.gossiperl.client.serialization.DeserializeResultError;
import com.gossiperl.client.serialization.Serializer;
import org.apache.log4j.Logger;
import org.apache.thrift.TBase;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.HashMap;

public class Udp implements Runnable {

    private OverlayWorker worker;
    private DatagramSocket socket;

    private Serializer serializer;
    private Aes256 encryption;

    private static Logger LOG = Logger.getLogger(Udp.class);
    private static String IP_ADDRESS = "127.0.0.1";

    public Udp(OverlayWorker worker) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        this.worker = worker;
        this.serializer = new Serializer();
        this.encryption = new Aes256( this.worker.getConfiguration().getSymmetricKey() );
    }

    public void run() {
        try {
            this.socket = new DatagramSocket( this.worker.getConfiguration().getClientPort() );
            byte[] buffer = new byte[ this.worker.getConfiguration().getThriftWindowSize() ];
            LOG.info("[" + worker.getConfiguration().getClientName() + "] Datagram server started on port " + this.worker.getConfiguration().getClientPort() + ".");
            while (this.worker.isWorking()) {
                DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                try {
                    this.socket.receive(received);
                    byte[] packet = new byte[received.getLength()];
                    packet = Arrays.copyOfRange(received.getData(), received.getOffset(), received.getLength() + received.getOffset());
                    try {
                        byte[] decrypted = this.encryption.decrypt(packet);
                        DeserializeResult result = this.serializer.deserialize( decrypted );
                        this.worker.getMessaging().receive(result);
                    } catch (NoSuchAlgorithmException ex) {
                        this.worker.getMessaging().receive(new DeserializeResultError( new GossiperlClientException("Decryption error. No algorithm.", ex) ));
                    } catch (NoSuchPaddingException ex) {
                        this.worker.getMessaging().receive(new DeserializeResultError( new GossiperlClientException("Decryption error. No padding.", ex) ));
                    } catch (InvalidKeyException ex) {
                        this.worker.getMessaging().receive(new DeserializeResultError( new GossiperlClientException("Decryption error. Invalid key.", ex) ));
                    } catch (InvalidAlgorithmParameterException ex) {
                        this.worker.getMessaging().receive(new DeserializeResultError( new GossiperlClientException("Decryption error. Invalid algorithm.", ex) ));
                    } catch (IllegalBlockSizeException ex) {
                        this.worker.getMessaging().receive(new DeserializeResultError( new GossiperlClientException("Decryption error. Illegal block size.", ex) ));
                    } catch (BadPaddingException ex) {
                        this.worker.getMessaging().receive(new DeserializeResultError(new GossiperlClientException("Decryption error. Bad padding.", ex)));
                    } catch (NoSuchProviderException ex) {
                        this.worker.getMessaging().receive(new DeserializeResultError(new GossiperlClientException("Decryption error. Unsupported security provider.", ex)));
                    } catch (GossiperlClientException ex) {
                        this.worker.getMessaging().receive(new DeserializeResultError(ex));
                    }
                } catch (IOException ex) {
                    LOG.error("[" + worker.getConfiguration().getClientName() + "] Error while receiving datagram. Reason: ", ex);
                }
            }
        } catch (SocketException ex) {
            LOG.error("[" + worker.getConfiguration().getClientName() + "] Could not bind client socket on port" + worker.getConfiguration().getClientPort() + ". Reason: ", ex);
        }
    }

    public void send(HashMap<String, CustomDigestField> digestData) {
        TBase envelope = this.serializer.serializeArbitrary( digestData );
        this.send( envelope );
    }

    public void send(TBase digest) {
        try {
            LOG.debug("[" + worker.getConfiguration().getClientName() + "] Attempting sending " + digest.getClass().getName() + " to 127.0.0.1:" + this.worker.getConfiguration().getOverlayPort() + ".");
            byte[] serialized = this.serializer.serialize(digest);
            byte[] encrypted = this.encryption.encrypt(serialized);
            DatagramPacket sendPacket = new DatagramPacket(encrypted,
                    encrypted.length,
                    InetAddress.getByName(IP_ADDRESS),
                    this.worker.getConfiguration().getOverlayPort());
            this.socket.send(sendPacket);
            LOG.debug("[" + worker.getConfiguration().getClientName() + "] Digest of type " + digest.getClass().getName() + " sent to 127.0.0.1:" + this.worker.getConfiguration().getOverlayPort() + ".");
        } catch (UnknownHostException ex) {
            LOG.error("[" + worker.getConfiguration().getClientName() + "] Error while sending data to " + IP_ADDRESS + ". Reason: ", ex);
        } catch (IOException ex) {
            LOG.error("[" + worker.getConfiguration().getClientName() + "] Error while writing data to the UDP datagram. Reason: ", ex);
        } catch (GossiperlClientException ex) {
            LOG.error("[" + worker.getConfiguration().getClientName() + "] Error while serializing Thrift data. Reason: ", ex);
        } catch (NullPointerException ex) {
            LOG.error("[" + worker.getConfiguration().getClientName() + "] Socket not initialized. Did you start the thread? Reason: ", ex);
        } catch (Exception ex) {
            LOG.error("[" + worker.getConfiguration().getClientName() + "] Security exception. Reason: ", ex);
        }
    }

}
