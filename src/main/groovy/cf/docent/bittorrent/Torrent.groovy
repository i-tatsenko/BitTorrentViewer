package cf.docent.bittorrent
import cf.docent.bittorrent.protocol.NetDestination
import cf.docent.bittorrent.protocol.PeerMessageDispatcher
import cf.docent.bittorrent.protocol.bencode.SimpleBencodeDecoder
import cf.docent.bittorrent.protocol.handlers.BitFieldMessageHandler
import cf.docent.bittorrent.protocol.peer.PeerManager
import cf.docent.bittorrent.protocol.tracker.PeerResponse
import cf.docent.bittorrent.protocol.tracker.Tracker

import java.time.LocalTime
/**
 * Created by docent on 16.11.15.
 */
class Torrent {

    TorrentData torrentData
    PeerResponse peerResponse
    byte[] peerId
    private Tracker tracker
    private PeerManager peerManager
    private DataManager dataManager
    private PeerMessageDispatcher peerMessageDispatcher = new PeerMessageDispatcher()

    Torrent(File torrentMetaFile, Tracker tracker, byte[] peerId) {
        this.tracker = tracker
        torrentData = new TorrentData(torrentMetaFile, new SimpleBencodeDecoder())
        this.peerId = peerId
    }

    List<TorrentFile> listTorrentFiles() {
        return torrentData.torrentFiles
    }

    synchronized def connectPeers() {
        if (!peerManager) {
            peerManager = new PeerManager(peerId, torrentData.infoHash, this.&getSeeds, peerMessageDispatcher)
        }
        peerManager.connectPeers()
        dataManager = new DataManager(torrentData.pieces.size(), torrentData.pieceLength, peerManager, peerMessageDispatcher)
        peerMessageDispatcher.addListener(new BitFieldMessageHandler(dataManager))

    }

    byte[] getFileData(TorrentFile torrentFile) {
        return dataManager.getPiece(torrentFile.dataChunks.get(0).piece).get()
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
