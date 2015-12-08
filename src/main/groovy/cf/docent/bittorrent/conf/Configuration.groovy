package cf.docent.bittorrent.conf

import cf.docent.bittorrent.Util

/**
 * Created by docent on 19.11.15.
 */
class Configuration {

    def peerId = Util.randomString(20)
    String keyId = Util.randomString(9)
    int listeningPort = 12312

}
