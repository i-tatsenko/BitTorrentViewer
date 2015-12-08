package cf.docent.bittorrent.protocol.peer
import cf.docent.bittorrent.protocol.NetDestination
import cf.docent.bittorrent.protocol.peer.message.HandShakeMessage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Peer implements PeerConnectionStatusListener, PeerMessageListener {

    private static final Logger LOGGER = LogManager.getLogger(Peer)

    private PeerStatusListener statusListener
    private PeerConnection peerConnection
    boolean chocked

    static connect(NetDestination netDestination, PeerStatusListener statusListener) {
        def peer = new Peer()
        peer.statusListener = statusListener
        peer.peerConnection = PeerConnection.connect(netDestination, peer, peer)
    }

    def sendHandshake(byte[] infoHash, byte[] peerId) {
        peerConnection.sendToPeer new HandShakeMessage(infoHash, peerId)
    }

    def getDestination() {
        return peerConnection.destination
    }

    @Override
    void statusChanged(ConnectionStatus old, ConnectionStatus newStatus, PeerConnection seedConnection) {
        statusListener.statusChanged this, old, newStatus
    }

    @Override
    def onMessage(PeerMessage peerMessage) {
        LOGGER.debug("Received peerMessage: $peerMessage from ${peerConnection.destination}")
    }


}
