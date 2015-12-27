package cf.docent.bittorrent.conf

import cf.docent.bittorrent.Util
import org.springframework.stereotype.Component

@Component
class Configuration {

    public static final long DEFAULT_DATA_REQUEST_SIZE = 1 << 14;

    def peerId = '-DC0001-' + Util.randomNums(12)
    String keyId = Util.randomString(9)
    int listeningPort = 12312

    Configuration() {
        println "PeerId: $peerId"
    }

}
