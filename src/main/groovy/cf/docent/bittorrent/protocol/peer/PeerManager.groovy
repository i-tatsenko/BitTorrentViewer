package cf.docent.bittorrent.protocol.peer

import cf.docent.bittorrent.protocol.NetDestination
import cf.docent.bittorrent.protocol.PeerMessageDispatcher
import cf.docent.bittorrent.protocol.peer.message.ChokeMessage
import cf.docent.bittorrent.protocol.peer.message.UnchokeMessage
import io.netty.util.internal.ConcurrentSet
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.util.function.Supplier

class PeerManager implements PeerStatusListener, PeerMessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(PeerManager)

    final Set<Peer> connectedPeers = new ConcurrentSet<>()
    final Set<Peer> chockedPeers = new ConcurrentSet<>()
    final Set<Peer> failedPeers = new ConcurrentSet<>()
    final Set<Peer> disconnectedPeers = new ConcurrentSet<>()
    byte[] peerId
    byte[] infoHash
    private Supplier<Collection<NetDestination>> seedsProvider
    private PeerMessageDispatcher peerMessageDispatcher

    public PeerManager(byte[] peerId, byte[] infoHash, Supplier<Collection<NetDestination>> seedsProvider, PeerMessageDispatcher peerMessageDispatcher) {
        this.peerMessageDispatcher = peerMessageDispatcher
        this.seedsProvider = seedsProvider
        this.peerId = peerId
        this.infoHash = infoHash
    }

    def connectPeers() {
        seedsProvider.get()
                .findAll { !connectedPeers.contains(it) }
                .forEach { Peer.connect(it, this, peerMessageDispatcher) }
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

    def List<Peer> getAllConnectePeers() {
        def peers = new ArrayList<Peer>(connectedPeers)
        peers.addAll(chockedPeers)
        return peers
    }

    private void chokePeer(Peer peer) {
        synchronized (chockedPeers) {
            connectedPeers.remove(peer)
            chockedPeers << peer
        }
    }

    private void unChokePeer(Peer peer) {
        synchronized (chockedPeers) {
            connectedPeers << peer
            chockedPeers.remove(peer)
        }
    }

    @Override
    void onMessage(Peer peer, PeerMessage message) {
        if (message instanceof ChokeMessage) {
            chokePeer(peer)
        }
        else if (message instanceof UnchokeMessage) {
            unChokePeer(peer)
        }
    }

    @Override
    Set<Class> getHandledPeerMessages() {
        return [ChokeMessage, UnchokeMessage]
    }
}
