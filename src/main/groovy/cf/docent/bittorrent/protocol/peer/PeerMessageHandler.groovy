package cf.docent.bittorrent.protocol.peer

/**
 * Created by docent on 08.12.15.
 */
interface PeerMessageHandler {

    void onMessage(Peer peer, PeerMessage message)

    Set<Class> getHandledPeerMessages()

}