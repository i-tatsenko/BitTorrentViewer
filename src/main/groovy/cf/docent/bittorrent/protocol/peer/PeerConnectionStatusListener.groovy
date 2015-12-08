package cf.docent.bittorrent.protocol.peer

import groovy.transform.PackageScope

/**
 * Created by docent on 20.11.15.
 */
@PackageScope
interface PeerConnectionStatusListener {

    void statusChanged(ConnectionStatus old, ConnectionStatus newStatus, PeerConnection seedConnection)
}
