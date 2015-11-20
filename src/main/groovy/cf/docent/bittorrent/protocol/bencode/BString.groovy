package cf.docent.bittorrent.protocol.bencode

import cf.docent.bittorrent.protocol.NetDestination

/**
 * Created by Yasha on 22.10.2015.
 */
class BString {
    byte[] bytes

    String toString() {
        new String(bytes)
    }

    def toNetDestinations() {
        NetDestination.createNetDestinationList(bytes)
    }

    Object asType(Class toClass) {
        if (toClass == String) {
            return toString()
        }
        return this
    }
}
