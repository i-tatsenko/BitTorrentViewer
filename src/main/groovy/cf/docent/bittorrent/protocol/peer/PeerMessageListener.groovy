package cf.docent.bittorrent.protocol.peer

/**
 * Created by docent on 29.11.15.
 */
interface PeerMessageListener {

    def onMessage(PeerMessage peerMessage)
}
