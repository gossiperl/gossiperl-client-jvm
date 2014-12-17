package com.gossiperl.client.transport;

import com.gossiperl.client.GossiperlClientException;
import com.gossiperl.client.OverlayWorker;
import com.gossiperl.client.encryption.Aes256;
import com.gossiperl.client.serialization.Serializer;
import org.apache.thrift.TBase;

import java.io.IOException;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Udp implements Runnable {

    private OverlayWorker worker;
    private DatagramSocket socket;
    private ConcurrentLinkedQueue<TransportMessage> queue;

    private Serializer serializer;
    private Aes256 encryption;

    public Udp(OverlayWorker worker, ConcurrentLinkedQueue<TransportMessage> queue) throws NoSuchAlgorithmException {
        this.worker = worker;
        this.queue = queue;
        this.serializer = new Serializer();
        this.encryption = new Aes256( this.worker.getConfiguration().getSymmetricKey() );
    }

    public void run() {
        try {
            this.socket = new DatagramSocket( this.worker.getConfiguration().getClientPort() );
            byte[] buffer = new byte[ this.worker.getConfiguration().getThriftWindowSize() ];
            while (this.worker.isWorking()) {
                DatagramPacket received = new DatagramPacket(buffer, buffer.length);
                try {
                    this.socket.receive(received);
                    byte[] packet = new byte[received.getLength()];
                    packet = Arrays.copyOfRange(received.getData(), received.getOffset(), received.getLength() + received.getOffset());
                    this.queue.offer( new TransportMessage( packet ) );
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (SocketException ex) {
            // TODO: log
            ex.printStackTrace();
        }
    }

    public void send(TBase digest) {
        try {
            byte[] serialized = this.serializer.serialize(digest);
            byte[] encrypted = this.encryption.encrypt(serialized);
            DatagramPacket sendPacket = new DatagramPacket(encrypted,
                    encrypted.length,
                    InetAddress.getByName("127.0.0.1"),
                    this.worker.getConfiguration().getOverlayPort());
            // TODO: these digests should end up on a separate blocking queue and be processed sequentially:
            this.socket.send(sendPacket);
        } catch (UnknownHostException ex) {
            // TODO: log
            ex.printStackTrace();
        } catch (IOException ex) {
            // TODO: log
            ex.printStackTrace();
        } catch (GossiperlClientException ex) {
            // TODO: log
            ex.printStackTrace();
        } catch (Exception ex) {
            // security exceptions:
            // TODO: log
            ex.printStackTrace();
        }
    }

}
