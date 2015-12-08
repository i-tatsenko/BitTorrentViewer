package cf.docent.bittorrent.protocol.peer

/**
 * Created by docent on 08.12.15.
 */
interface PeerStatusListener {

    def statusChanged(Peer peer, ConnectionStatus old_, ConnectionStatus new_)

}