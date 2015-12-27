package cf.docent.bittorrent.protocol.tracker
import cf.docent.bittorrent.protocol.NetDestination
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.time.LocalTime
/**
 * Created by docent on 19.11.15.
 */
class PeerResponse {

    private static final Logger LOGGER = LogManager.getLogger(PeerResponse)

    int complete
    int incomplete
    List<NetDestination> seeds
    LocalTime lastRequestTime
    LocalTime nextUpdate

    PeerResponse(def decodedData) {
        complete = decodedData.complete ?: 0
        incomplete = decodedData.incomplete ?: 0
        seeds = decodedData.peers.toNetDestinations()
        lastRequestTime = LocalTime.now()
        nextUpdate = lastRequestTime.plusSeconds(decodedData.interval as long)
    }

    def peers() {
        return seeds
    }
}
