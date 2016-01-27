package cf.docent.bittorrent.protocol

import cf.docent.bittorrent.protocol.peer.Peer
import cf.docent.bittorrent.protocol.peer.PeerMessage
import cf.docent.bittorrent.protocol.peer.PeerMessageHandler
import io.netty.util.internal.ConcurrentSet
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PeerMessageDispatcher {

    private static final Logger LOGGER = LogManager.getLogger(PeerMessageDispatcher)
    def peerMessageListeners = [:]

    def addListener(PeerMessageHandler listener) {
        listener.handledPeerMessages.each {
            def listeners = getListeners(it)
            listeners << listener
        }
    }

    def registerMessage(Peer peer, PeerMessage peerMessage) {
        if (peerMessage.needeToPrintToLog) LOGGER.debug("Received message from ${peer.destination} message: $peerMessage")
        getListeners(peerMessage.class).each { it.onMessage(peer, peerMessage) }

    }

    private Set<PeerMessageHandler> getListeners(Class messageClass) {
        def listeners = peerMessageListeners[messageClass]
        if (listeners == null) {
            synchronized (this) {
                listeners = peerMessageListeners[messageClass]
                if (listeners == null) {
                    listeners = new ConcurrentSet()
                    peerMessageListeners[messageClass] = listeners
                }
            }
        }
        return listeners
    }
}
