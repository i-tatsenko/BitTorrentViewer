package cf.docent.bittorrent.protocol.peer
import cf.docent.bittorrent.protocol.NetDestination
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class Peer implements PeerConnectionStatusListener, PeerMessageListener {

    private static final Logger LOGGER = LogManager.getLogger(Peer)

    private PeerConnectionStatusListener statusListener
    private PeerConnection peerConnection

    static connect(NetDestination netDestination, PeerConnectionStatusListener statusListener) {
        def peer = new Peer()
        peer.statusListener = statusListener
        peer.peerConnection = PeerConnection.connect(netDestination, peer, peer)
    }

    @Override
    void statusChanged(ConnectionStatus old, ConnectionStatus newStatus, PeerConnection seedConnection) {
        statusListener.statusChanged old, newStatus, seedConnection
    }

    @Override
    def onMessage(PeerMessage peerMessage) {
        LOGGER.debug("Received peerMessage: $peerMessage from ${peerConnection.destination}")
    }


}
