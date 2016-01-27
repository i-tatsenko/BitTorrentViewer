package cf.docent.bittorrent.protocol.download

import cf.docent.bittorrent.data.DataChunk
import cf.docent.bittorrent.data.Piece
import cf.docent.bittorrent.data.PieceAvailability
import cf.docent.bittorrent.protocol.PeerMessageDispatcher
import cf.docent.bittorrent.protocol.peer.Peer
import cf.docent.bittorrent.protocol.peer.PeerManager
import cf.docent.bittorrent.protocol.peer.PeerMessage
import cf.docent.bittorrent.protocol.peer.PeerMessageHandler
import cf.docent.bittorrent.protocol.peer.message.BitFieldMessage
import cf.docent.bittorrent.protocol.peer.message.HaveMessage
import cf.docent.bittorrent.protocol.peer.message.InterestedMessage
import cf.docent.bittorrent.protocol.peer.message.PieceMessage
import cf.docent.bittorrent.protocol.peer.message.RequestMessage
import cf.docent.bittorrent.protocol.peer.message.UnchokeMessage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import javax.sql.DataSource
import java.time.LocalTime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future

import static cf.docent.bittorrent.conf.Configuration.DEFAULT_DATA_REQUEST_SIZE

class DownloadManager implements PeerMessageHandler {

    private static final Logger LOGGER = LogManager.getLogger(DownloadManager)

    private PeerManager peerManager
    private final PeerProgressManager peerProgressManager
    private final List<ChunkRequest> chunksToDownload = new ArrayList<>(10_000)
    private final Map<Peer, PieceAvailability> dataAvailability = new ConcurrentHashMap<>()
    private final Map<Long, PieceAwaiting> awaitingPieces = new ConcurrentHashMap<>()
    private final long pieceCount

    public DownloadManager(PeerManager peerManager, DataSource dataSource, PeerMessageDispatcher messageDispatcher, long piecesCount) {
        peerProgressManager = new PeerProgressManager(dataSource)
        messageDispatcher.addListener(this)
        this.peerManager = peerManager
        this.pieceCount = piecesCount
    }

    Future<byte[]> downloadPiece(Piece piece) {
        def chunks = pieceToChunks(piece)
        synchronized (chunksToDownload) {
            chunksToDownload.addAll(chunks)
        }
        def pa = new PieceAwaiting(chunks.size(), piece.pieceLength)
        awaitingPieces[(piece.index)] = pa
        sendChunkRequests(peerManager.getConnectedPeers())
        return pa.promise
    }

    def sendChunkRequests(Set<Peer> peers) {
        Map<Peer, Integer> peersAvailable = peerProgressManager.getPeersWithNotFullDownloadQueue(peers)
        peersAvailable.each { peer, maxReqChunks ->
            peer.sendMessage(new UnchokeMessage())
            peer.sendMessage(new InterestedMessage())
            (1..maxReqChunks).each {
                def toDownload = getNextChunkToDownload(peer)
                sendRequest(peer, toDownload)
            }
        }
    }

    def sendRequest(Peer peer, DataChunk dataChunk) {
        if (!dataChunk) {
            return
        }
        peerProgressManager.markDownloadRequestSent(peer, dataChunk)
        peer.sendMessage new RequestMessage(dataChunk.piece.index, dataChunk.offset, dataChunk.length)
    }

    private DataChunk getNextChunkToDownload(Peer peer) {
        def piecesForPeer = dataAvailability.get(peer)
        if (!piecesForPeer) {
            return null
        }
        synchronized (chunksToDownload) {

            def index = chunksToDownload.findIndexOf { piecesForPeer.isPieceAvailable(it.chunk.piece.index) }
            if (index == -1) {
                return getDownloadingChunk(peer)
            }

            return chunksToDownload.remove(index).chunk
        }
    }

    private DataChunk getDownloadingChunk(Peer peer) {
        peerProgressManager.getChunkToDownload(peer, dataAvailability.get(peer))
    }

    static def List<ChunkRequest> pieceToChunks(Piece piece) {
        int pieceLengthLeft = piece.pieceLength
        int offset = 0
        long chunksCount = 0
        List<ChunkRequest> result = []
        while (pieceLengthLeft > 0) {
            chunksCount++
            int newChunkLength = pieceLengthLeft
            if (pieceLengthLeft >= DEFAULT_DATA_REQUEST_SIZE) {
                newChunkLength = DEFAULT_DATA_REQUEST_SIZE
            }
            result << new ChunkRequest(chunk: new DataChunk(piece, offset, newChunkLength), requestSentTime: LocalTime.now())
            offset += newChunkLength
            pieceLengthLeft -= newChunkLength
        }
        return result
    }

    @Override
    void onMessage(Peer peer, PeerMessage message) {
        LOGGER.info("Don't know what to do wuth message: ${message.class}")
    }

    def onMessage(Peer peer, UnchokeMessage unchokeMessage) {
        sendChunkRequests([peer] as Set)
    }

    def onMessage(Peer peer, BitFieldMessage bitFieldMessage) {
        dataAvailability.put(peer, new PieceAvailability(bitFieldMessage.bytes))
    }

    def onMessage(Peer peer, HaveMessage haveMessage) {
        def pa = dataAvailability.get(peer)
        if (!pa) {
            pa = new PieceAvailability(new byte[pieceCount])
            def newPA = dataAvailability.putIfAbsent(peer, pa)
            if(newPA) {
                pa = newPA
            }
        }

        pa.markPieceAvailable(haveMessage.readFirstInt())
    }

    def onMessage(Peer peer, PieceMessage pieceMessage) {
        peerProgressManager.markDownloaded(peer, pieceMessage.pieceIndex(),
                pieceMessage.offset(), pieceMessage.dataLength)
        def pa = awaitingPieces.get(pieceMessage.pieceIndex())
        if (pa == null) {
            LOGGER.debug("No pa for chunk with index ${pieceMessage.pieceIndex()}")
            return
        }
        if (pa.completeChunk(pieceMessage.offset(), pieceMessage.data())) {
            awaitingPieces.remove(pieceMessage.pieceIndex())
        }
        sendChunkRequests([peer] as Set)
        printStatus()
    }

    def printStatus() {
        println "==="
        awaitingPieces.each {println "Piece index: ${it.key} left pieces: ${it.value.chunksCount.get()}"}
    }


    @Override
    Set<Class> getHandledPeerMessages() {
        return [UnchokeMessage, BitFieldMessage, HaveMessage, PieceMessage]
    }
}
