package cf.docent.bittorrent

import cf.docent.bittorrent.protocol.NetDestination
import cf.docent.bittorrent.protocol.bencode.SimpleBencodeDecoder
import cf.docent.bittorrent.protocol.tracker.PeerResponse
import cf.docent.bittorrent.protocol.tracker.Tracker

import java.time.LocalTime

/**
 * Created by docent on 16.11.15.
 */
class Torrent {

    TorrentData torrentData
    PeerResponse peerResponse
    private Tracker tracker

    Torrent(File torrentMetaFile, Tracker tracker) {
        this.tracker = tracker
        torrentData = new TorrentData(torrentMetaFile, new SimpleBencodeDecoder())
    }

    List<TorrentFile> listTorrentFiles() {
        return torrentData.torrentFiles
    }

    List<NetDestination> getSeeds() {
        updateIfNeeded()
        return peerResponse.seeds
    }

    private synchronized def updateIfNeeded() {
        if (needDataUpdateFromTracker()) {
            peerResponse = tracker.requestPeers(torrentData)
        }
    }

    private boolean needDataUpdateFromTracker() {
        return peerResponse == null || peerResponse.lastRequestTime.isBefore(LocalTime.now())
    }


    def infoHash() {
        torrentData.infoHash
    }

    @Override
    String toString() {
        """Torrent[announce: ${torrentData.announce} name: ${torrentData.info.name}
piece length: ${torrentData.pieceLength()} pieces count: ${torrentData.pieces().size()} files: ${listTorrentFiles()}"""
    }
}
