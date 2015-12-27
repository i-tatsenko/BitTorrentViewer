package cf.docent.bittorrent.protocol.handlers
import cf.docent.bittorrent.DataManager
import cf.docent.bittorrent.protocol.peer.Peer
import cf.docent.bittorrent.protocol.peer.PeerMessage
import cf.docent.bittorrent.protocol.peer.PeerMessageHandler
import cf.docent.bittorrent.protocol.peer.message.BitFieldMessage

class BitFieldMessageHandler implements PeerMessageHandler {

    private final DataManager dataManager

    BitFieldMessageHandler(DataManager dataManager) {
        this.dataManager = dataManager
    }

    @Override
    void onMessage(Peer peer, PeerMessage message) {
        dataManager.markPiecesAvailable peer, message.messageBytes
    }

    @Override
    Set<Class> getHandledPeerMessages() {
        return [BitFieldMessage]
    }
}
