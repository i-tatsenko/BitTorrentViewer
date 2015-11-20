package cf.docent.bittorrent

import cf.docent.bittorrent.protocol.bencode.SimpleBencodeDecoder
import cf.docent.bittorrent.protocol.tracker.PeerResponse
import cf.docent.bittorrent.protocol.tracker.Tracker

/**
 * Created by docent on 16.11.15.
 */
class Torrent {

    TorrentData torrentData
    PeerResponse peerResponse
    def fileList = []

    Torrent(File torrentMetaFile, Tracker tracker) {
        torrentData = new TorrentData(torrentMetaFile, new SimpleBencodeDecoder())
        peerResponse = tracker.requestPeers(torrentData)
    }

    List<TorrentFile> listTorrentFiles(){
        return torrentData.torrentFiles
    }

    def getSeedsCount() {
        return peerResponse?.seedersCount()
    }

    def getLeechersCount() {
        return peerResponse?.leechersCount()
    }

    @Override
    String toString() {
        """Torrent[announce: ${torrentData.announce} name: ${torrentData.info.name}
piece length: ${torrentData.pieceLength()} files: ${listTorrentFiles()}
seeders: $seedsCount leechers: $leechersCount]"""
    }
}
