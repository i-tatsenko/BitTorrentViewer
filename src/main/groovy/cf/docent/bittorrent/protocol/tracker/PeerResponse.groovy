package cf.docent.bittorrent.protocol.tracker

import cf.docent.bittorrent.protocol.NetDestination

/**
 * Created by docent on 19.11.15.
 */
class PeerResponse {

    int complete
    int incomplete
    List<NetDestination> seeds

    PeerResponse(def decodedData) {
        complete = decodedData.complete ?: 0
        incomplete = decodedData.incomplete ?: 0
        seeds = decodedData.peers.toNetDestinations()
    }

    def seedersCount() {
        return complete
    }

    def leechersCount() {
        return incomplete
    }

    def peers() {
        return seeds
    }
}
