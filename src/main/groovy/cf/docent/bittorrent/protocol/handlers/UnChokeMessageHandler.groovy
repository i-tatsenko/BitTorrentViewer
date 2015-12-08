package cf.docent.bittorrent.protocol.handlers
import cf.docent.bittorrent.protocol.peer.Peer
import cf.docent.bittorrent.protocol.peer.PeerMessageHandler
import cf.docent.bittorrent.protocol.peer.message.UnchokeMessage
/**
 * Created by docent on 08.12.15.
 */
class UnChokeMessageHandler implements PeerMessageHandler<UnchokeMessage> {

    @Override
    void onMessage(Peer peer, UnchokeMessage message) {
        peer.chocked = false
    }
}
