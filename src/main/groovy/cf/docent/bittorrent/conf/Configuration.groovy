package cf.docent.bittorrent.conf

import cf.docent.bittorrent.Util

/**
 * Created by docent on 19.11.15.
 */
class Configuration {

    String getPeerId() {
        def peerId = Util.randomString(20)
        println "Peer id $peerId length ${peerId.length()}"
        return peerId.toString()
    }

    String getKeyId() {
        Util.randomString(9)
    }

    int getListeningPort() {
        return 12312
    }
}
