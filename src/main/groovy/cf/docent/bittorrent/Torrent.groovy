package cf.docent.bittorrent

import cf.docent.bittorrent.conf.Configuration
import cf.docent.bittorrent.protocol.bencode.SimpleBencodeDecoder
import cf.docent.bittorrent.protocol.peer.PeerManager
import cf.docent.bittorrent.protocol.tracker.PeerResponse
import cf.docent.bittorrent.protocol.tracker.Tracker

/**
 * Created by docent on 16.11.15.
 */
class Torrent {

    TorrentData torrentData
    PeerResponse peerResponse
    def fileList = []
    private PeerManager peerManager

    Torrent(File torrentMetaFile, Tracker tracker, Configuration configuration) {
        torrentData = new TorrentData(torrentMetaFile, new SimpleBencodeDecoder())
        peerResponse = tracker.requestPeers(torrentData)
        peerManager = new PeerManager(configuration, this)
        peerManager.connectPeersList(peerResponse.seeds)
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

    def infoHash() {
        torrentData.infoHash
    }

    @Override
    String toString() {
        """Torrent[announce: ${torrentData.announce} name: ${torrentData.info.name}
piece length: ${torrentData.pieceLength()} files: ${listTorrentFiles()}
seeders: $seedsCount leechers: $leechersCount]"""
    }
}
