package cf.docent.bittorrent.protocol.peer

/**
 * Created by docent on 08.12.15.
 */
interface PeerMessageHandler<T extends PeerMessage> {

    void onMessage(Peer peer, T message)

}