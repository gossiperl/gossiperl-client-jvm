package com.gossiperl.client.listener;

import com.gossiperl.client.exceptions.GossiperlClientException;
import com.gossiperl.client.OverlayWorker;
import org.apache.thrift.TBase;

import java.util.List;

public interface GossiperlClientListener {

    void connected(OverlayWorker worker);
    void disconnected(OverlayWorker worker);
    void event( OverlayWorker worker, String eventType, Object member, long heartbeat );
    void subscribeAck( OverlayWorker worker, List<String> events );
    void unsubscribeAck( OverlayWorker worker, List<String> events );
    void forwardAck( OverlayWorker worker, String reply_id );
    void forwarded( OverlayWorker worker, String digestType, TBase digest );
    void failed( OverlayWorker worker, GossiperlClientException error );

}
