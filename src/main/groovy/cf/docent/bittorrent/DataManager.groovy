package cf.docent.bittorrent
import cf.docent.bittorrent.data.DataChunk
import cf.docent.bittorrent.data.Piece
import cf.docent.bittorrent.data.PieceAvailability
import cf.docent.bittorrent.protocol.PeerMessageDispatcher
import cf.docent.bittorrent.protocol.peer.Peer
import cf.docent.bittorrent.protocol.peer.PeerManager
import cf.docent.bittorrent.protocol.peer.PeerMessage
import cf.docent.bittorrent.protocol.peer.PeerMessageHandler
import cf.docent.bittorrent.protocol.peer.message.InterestedMessage
import cf.docent.bittorrent.protocol.peer.message.PieceMessage
import cf.docent.bittorrent.protocol.peer.message.RequestMessage
import cf.docent.bittorrent.protocol.peer.message.UnchokeMessage

import java.time.LocalTime
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future

import static cf.docent.bittorrent.conf.Configuration.DEFAULT_DATA_REQUEST_SIZE
/**
 * Created by docent on 08.12.15.
 */
class DataManager implements PeerMessageHandler {

    final long piecesCount
    final long pieceLength
    Map<Peer, PieceAvailability> dataAvailability = new ConcurrentHashMap<Peer, PieceAvailability>()

    Map<Long, PieceAwaiting> processingPieces = new ConcurrentHashMap<>()
    private PeerManager peerManager

    DataManager(long piecesCount, long pieceLength, PeerManager peerManager, PeerMessageDispatcher messageDispatcher) {
        this.peerManager = peerManager
        this.piecesCount = piecesCount
        this.pieceLength = pieceLength
        messageDispatcher.addListener(this)
    }

    void markPiecesAvailable(Peer peer, byte[] piecesMask) {
        dataAvailability.put(peer, new PieceAvailability(piecesMask))
    }

    Future<byte[]> getPiece(Piece piece) {
        int pieceLengthLeft = piece.pieceLength
        int offset = 0
        def chunkRequests = []
        peerManager.allConnectePeers.each {
            it.sendMessage(new UnchokeMessage())
            it.sendMessage(new InterestedMessage())
        }
        while (pieceLengthLeft > 0) {
            int newChunkLength = pieceLengthLeft
            if (pieceLengthLeft >= DEFAULT_DATA_REQUEST_SIZE) {
                newChunkLength = DEFAULT_DATA_REQUEST_SIZE
            }
            chunkRequests << new ChunkRequest(chunk: new DataChunk(piece, offset, newChunkLength), requestSentTime: LocalTime.now())
            offset += newChunkLength
            pieceLengthLeft -= newChunkLength
        }
        def future = new CompletableFuture<byte[]>()
        def awaiting = new PieceAwaiting(promise: future, chunks: chunkRequests)
        processingPieces.put piece.index, awaiting

        sendRequestsToPeers peerManager.allConnectePeers.findAll {dataAvailability.get(it)?.isPieceAvailable(piece.index)}, chunkRequests
        return future
    }

    def static sendRequestsToPeers(Collection<Peer> peers, Collection<ChunkRequest> requests) {
        List<ChunkRequest> requestsLeft = new ArrayList<>(requests)
        int requestsPerPeer = requests.size() / peers.size()
        peers.each { peer ->
            (1..requestsPerPeer).each {
                if (requestsLeft.isEmpty()) {
                    return
                }
                ChunkRequest request = requestsLeft.remove(0)
                peer.sendMessage new RequestMessage(request.chunk.piece.index, request.chunk.offset, request.chunk.length)
            }
        }
    }

    void markPieceAvailable(Peer peer, int pieceIndex) {
        if (!(0..<pieceIndex).contains(pieceIndex)) {
            throw new IllegalArgumentException("Invalid piece index: $pieceIndex. Pieces count: $piecesCount")
        }

        PieceAvailability pieceAvailability = dataAvailability.get(peer)
        if (pieceAvailability == null) {
            pieceAvailability = new PieceAvailability(new byte[piecesCount])
            dataAvailability.putIfAbsent(peer, pieceAvailability)
            markPieceAvailable(peer, pieceIndex)
        } else {
            pieceAvailability.markPieceAvailable(pieceIndex)
        }
    }

    @Override
    void onMessage(Peer peer, PeerMessage message) {
        PieceMessage pieceMessage = message
        def pa = processingPieces.get(pieceMessage.pieceIndex())
        if (pa == null) {
            return
        }
        if (pa.completeChunk(pieceMessage.offset(), pieceMessage.data())) {
            processingPieces.remove(pieceMessage.pieceIndex())
        }
    }

    @Override
    Set<Class> getHandledPeerMessages() {
        return [PieceMessage]
    }
}

class PieceAwaiting {
    CompletableFuture<byte[]> promise
    Set<ChunkRequest> chunks
    byte[] data

    def boolean completeChunk(int offset, byte[] data) {
        def readyChunk = chunks.find { it.chunk.offset }
        synchronized (chunks) {
            chunks.remove(readyChunk)
        }
        System.arraycopy(data, 0, this.data, offset, (int) readyChunk.chunk.length)
        if (chunks.isEmpty()) {
            promise.complete(data)
        }
        return promise.done
    }
}

class ChunkRequest {
    DataChunk chunk
    LocalTime requestSentTime
}