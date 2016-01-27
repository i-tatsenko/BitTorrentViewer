package cf.docent.bittorrent
import cf.docent.bittorrent.protocol.NetDestination
import cf.docent.bittorrent.protocol.PeerMessageDispatcher
import cf.docent.bittorrent.protocol.bencode.SimpleBencodeDecoder
import cf.docent.bittorrent.protocol.download.DownloadManager
import cf.docent.bittorrent.protocol.peer.PeerManager
import cf.docent.bittorrent.protocol.tracker.PeerResponse
import cf.docent.bittorrent.protocol.tracker.Tracker

import javax.sql.DataSource
import java.time.LocalTime

class Torrent {

    TorrentData torrentData
    PeerResponse peerResponse
    byte[] peerId
    private Tracker tracker
    private PeerManager peerManager
    private DownloadManager downloadManager
    private DataSource dataSource
    private PeerMessageDispatcher peerMessageDispatcher = new PeerMessageDispatcher()

    Torrent(File torrentMetaFile, Tracker tracker, byte[] peerId, DataSource dataSource) {
        this.tracker = tracker
        torrentData = new TorrentData(torrentMetaFile, new SimpleBencodeDecoder())
        this.peerId = peerId
        this.dataSource = dataSource
    }

    List<TorrentFile> listTorrentFiles() {
        return torrentData.torrentFiles
    }

    synchronized def connectPeers() {
        if (!peerManager) {
            peerManager = new PeerManager(peerId, torrentData.infoHash, this.&getSeeds, peerMessageDispatcher)
        }
        peerManager.connectPeers()
        downloadManager = new DownloadManager(peerManager, dataSource, peerMessageDispatcher, torrentData.pieces.size())
    }

    byte[] getFileData(TorrentFile torrentFile) {
        List<byte[]> bytes = (0..50).collect({ downloadManager.downloadPiece(torrentFile.dataChunks.get(it).piece) })
                .collect({ it.get() })
        int resultLength = bytes.inject 0, {s, v -> s + v.length}
        byte[] result = new byte[resultLength]
        bytes.eachWithIndex { byte[] data, i -> System.arraycopy(data, 0, result, data.length * i, data.length) }
        return result
    }

    private List<NetDestination> getSeeds() {
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
piece length: ${torrentData.pieceLength} pieces count: ${torrentData.pieces.size()} files: ${listTorrentFiles()}"""
    }
}
