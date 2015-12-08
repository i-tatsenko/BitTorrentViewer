package cf.docent.bittorrent.protocol.peer

import cf.docent.bittorrent.Torrent
import cf.docent.bittorrent.conf.Configuration
import io.netty.util.internal.ConcurrentSet
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PeerManager implements PeerStatusListener {

    private static final Logger LOGGER = LogManager.getLogger(PeerManager)

    Set<Peer> connectedPeers = new ConcurrentSet<>()
    Set<Peer> failedPeers = new ConcurrentSet<>()
    Set<Peer> disconnectedPeers = new ConcurrentSet<>()
    byte[] peerId
    byte[] infoHash
    private Torrent torrent

    public PeerManager(Configuration configuration, Torrent torrent) {
        this.torrent = torrent
        peerId = configuration.peerId.bytes
        infoHash = torrent.infoHash()
    }

    def connectPeers() {
        torrent.seeds
                .findAll { !connectedPeers.contains(it) }
                .forEach { Peer.connect(it, this) }
    }

    @Override
    def statusChanged(Peer peer, ConnectionStatus old, ConnectionStatus newStatus) {
        if (newStatus == ConnectionStatus.CONNECTED) {
            connectedPeers << peer
            LOGGER.debug("Connected to ${peer.destination}")
            peer.sendHandshake(infoHash, peerId)
        }
        if (newStatus == ConnectionStatus.DISCONNECTED) {
            connectedPeers.remove(peer)
            disconnectedPeers << peer
        }
        if (newStatus == ConnectionStatus.CONNECTION_FAILED) {
            connectedPeers.remove(peer)
            failedPeers << peer
        }
    }
}
