package cf.docent.bittorrent.protocol.peer

import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
/**
 * Created by docent on 20.11.15.
 */
trait PeerConnectionStatusListener {

    private static final Logger LOGGER = LogManager.getLogger(PeerConnectionStatusListener)

    public void statusChanged(ConnectionStatus old, ConnectionStatus newStatus, SeedConnection seedConnection) {
        LOGGER.debug("$seedConnection status has been changed from $old to $newStatus")
    }
}
