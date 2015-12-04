package cf.docent.bittorrent

import cf.docent.bittorrent.conf.Configuration
import cf.docent.bittorrent.protocol.tracker.Tracker;

/**
 * Created by docent on 16.11.15.
 */
public class TorrentManager {

    Configuration configuration = new Configuration()

    Torrent addTorrent(File torrentFile) {
        return new Torrent(torrentFile, new Tracker(configuration), configuration)
    }

}
