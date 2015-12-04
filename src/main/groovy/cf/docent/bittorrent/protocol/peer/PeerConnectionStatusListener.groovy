package cf.docent.bittorrent.protocol.peer
/**
 * Created by docent on 20.11.15.
 */
interface PeerConnectionStatusListener {

    void statusChanged(ConnectionStatus old, ConnectionStatus newStatus, PeerConnection seedConnection)
}
