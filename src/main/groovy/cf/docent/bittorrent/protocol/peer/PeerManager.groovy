package cf.docent.bittorrent.protocol.peer

import cf.docent.bittorrent.Torrent
import cf.docent.bittorrent.conf.Configuration
import cf.docent.bittorrent.protocol.NetDestination
import cf.docent.bittorrent.protocol.peer.message.HandShakeMessage
import io.netty.util.internal.ConcurrentSet
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PeerManager implements PeerConnectionStatusListener {

    private static final Logger LOGGER = LogManager.getLogger()

    Set<PeerConnection> connectedPeers = new ConcurrentSet<>()
    Set<PeerConnection> failedPeers = new ConcurrentSet<>()
    Set<PeerConnection> disconnectedPeers = new ConcurrentSet<>()
    byte[] peerId
    byte[] infoHash

    public PeerManager(Configuration configuration, Torrent torrent) {
        peerId = configuration.peerId.bytes
        infoHash = torrent.infoHash()
    }

    def connectPeersList(List<NetDestination> destinations){
        destinations.each {Peer.connect(it, this)}
    }

    @Override
    void statusChanged(ConnectionStatus old, ConnectionStatus newStatus, PeerConnection seedConnection) {
        if (newStatus == ConnectionStatus.CONNECTED) {
            connectedPeers << seedConnection
            LOGGER.debug("Connected to ${seedConnection.destination}")
            seedConnection.sendToPeer(new HandShakeMessage(infoHash, peerId))
        }
        if (newStatus == ConnectionStatus.DISCONNECTED) {
            connectedPeers.remove(seedConnection)
            disconnectedPeers << seedConnection
        }
        if (newStatus == ConnectionStatus.CONNECTION_FAILED) {
            connectedPeers.remove(seedConnection)
            failedPeers << seedConnection
        }
    }
}
