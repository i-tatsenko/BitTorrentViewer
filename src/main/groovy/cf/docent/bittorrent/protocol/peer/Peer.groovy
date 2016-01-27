package cf.docent.bittorrent.protocol.peer
import cf.docent.bittorrent.protocol.NetDestination
import cf.docent.bittorrent.protocol.PeerMessageDispatcher
import cf.docent.bittorrent.protocol.peer.message.HandShakeMessage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Peer implements PeerConnectionStatusListener {

    private static final Logger LOGGER = LogManager.getLogger(Peer)

    private PeerStatusListener statusListener
    private PeerConnection peerConnection

    static connect(NetDestination netDestination, PeerStatusListener statusListener, PeerMessageDispatcher peerMessageDispatcher) {
        def peer = new Peer()
        peer.statusListener = statusListener
        peer.peerConnection = PeerConnection.connect(netDestination, peer, {peerMessageDispatcher.registerMessage(peer, it)})
    }

    def sendHandshake(byte[] infoHash, byte[] peerId) {
        peerConnection.sendToPeer new HandShakeMessage(infoHash, peerId)
    }

    def sendMessage(PeerMessage peerMessage) {
        peerConnection.sendToPeer(peerMessage)
    }

    def getDestination() {
        return peerConnection.destination
    }

    @Override
    void statusChanged(ConnectionStatus old, ConnectionStatus newStatus, PeerConnection seedConnection) {
        statusListener.statusChanged this, old, newStatus
    }

    @Override
    String toString() {
        peerConnection.destination.toString()
    }
}
