package cf.docent.bittorrent
import cf.docent.bittorrent.conf.Configuration
import cf.docent.bittorrent.protocol.tracker.Tracker
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DefaultTorrentManager implements TorrentManager {

    @Autowired
    private Configuration configuration

    @Override
    Torrent addTorrent(File torrentFile) {
        return new Torrent(torrentFile, new Tracker(configuration), configuration.peerId.bytes)
    }

}
