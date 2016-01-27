package cf.docent.bittorrent.protocol.download

import cf.docent.bittorrent.data.DataChunk
import cf.docent.bittorrent.data.Piece
import cf.docent.bittorrent.data.PieceAvailability
import cf.docent.bittorrent.protocol.peer.Peer
import groovy.sql.Sql

import javax.sql.DataSource

class PeerProgressManager {
    private final int maxQueueLength = 15
    private final DataSource dataSource

    public PeerProgressManager(DataSource dataSource) {
        this.dataSource = dataSource
    }

    def Map<Peer, Integer> getPeersWithNotFullDownloadQueue(Set<Peer> peers) {
        peers.collectEntries { [(it): maxQueueLength - getChunksInProgressCount(it)] }
                .findAll { k, v -> v > 0 }
    }

    def int getChunksInProgressCount(Peer peer) {
        new Sql(dataSource).firstRow("SELECT count(1) AS c FROM progress WHERE peer = ? ", [peer.toString()])
                .c as Integer
    }

    DataChunk getChunkToDownload(Peer peer, PieceAvailability availability) {
        def result = new Sql(dataSource).rows("""SELECT piece_id, chunk_length, chunk_offset, count(*)
                                     FROM progress
                                     WHERE peer <> ${peer.toString()}
                                     GROUP BY piece_id, chunk_length, chunk_offset""")
                .collect { new DataChunk(new Piece(it.piece_id, -1, null), it.chunk_offset, it.chunk_length) };
        if (result) {
            result.find { availability.isPieceAvailable(it.piece.index) }
        }
        return null
    }

    def markDownloadRequestSent(Peer peer, DataChunk d) {

        new Sql(dataSource).execute("""INSERT INTO progress (peer, piece_id, chunk_length, chunk_offset)
                                       VALUES (?, ?, ?, ?)""", [peer.toString(), d.piece.index, d.length, d.offset])
    }

    def printDataForPeer(Peer peer) {
        new Sql(dataSource).execute("SELECT * FROM progress WHERE peer = ?", [peer.toString()], {b, rs-> println rs})
    }

    def markDownloaded(Peer peer, def pieceIndex, def offset, def length) {
//        println "State for peer ${peer} while deleting $pieceIndex $offset $length"
//        printDataForPeer(peer)
        new Sql(dataSource).execute("""DELETE FROM progress
                                       WHERE peer = ?
                                       AND piece_id = ?
                                       AND chunk_length = ?
                                       AND chunk_offset =? """,
                [peer.toString(), pieceIndex, length, offset])
    }


}


