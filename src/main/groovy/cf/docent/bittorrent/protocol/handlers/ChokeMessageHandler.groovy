package cf.docent.bittorrent.protocol.handlers

import cf.docent.bittorrent.protocol.peer.Peer
import cf.docent.bittorrent.protocol.peer.PeerMessageHandler
import cf.docent.bittorrent.protocol.peer.message.ChokeMessage

/**
 * Created by docent on 08.12.15.
 */
class ChokeMessageHandler implements PeerMessageHandler<ChokeMessage> {

    @Override
    void onMessage(Peer peer, ChokeMessage message) {
        peer.chocked = true
    }
}
