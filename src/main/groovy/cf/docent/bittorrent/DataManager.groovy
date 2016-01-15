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
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

import java.time.LocalTime
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Future
import java.util.concurrent.atomic.AtomicLong

import static cf.docent.bittorrent.conf.Configuration.DEFAULT_DATA_REQUEST_SIZE

class DataManager implements PeerMessageHandler {

    private static Logger LOGGER = LogManager.getLogger(DataManager)
    private static final int PEER_REQUEST_QUEUE_SIZE = 10


    final long piecesCount
    final long pieceLength
    Map<Peer, PieceAvailability> dataAvailability = new ConcurrentHashMap<Peer, PieceAvailability>()

    Map<Long, PieceAwaiting> processingPieces = new ConcurrentHashMap<>()
    Queue<ChunkRequest> pendingRequests = new ArrayBlockingQueue<>(100_000)
    ArrayBlockingQueue<ChunkRequest> sentRequests = new ArrayBlockingQueue<>(100_000)

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
        long chunksCount = 0
        peerManager.connectedPeers.each {
            it.sendMessage(new UnchokeMessage())
            it.sendMessage(new InterestedMessage())
        }
        while (pieceLengthLeft > 0) {
            chunksCount++
            int newChunkLength = pieceLengthLeft
            if (pieceLengthLeft >= DEFAULT_DATA_REQUEST_SIZE) {
                newChunkLength = DEFAULT_DATA_REQUEST_SIZE
            }
            pendingRequests << new ChunkRequest(chunk: new DataChunk(piece, offset, newChunkLength), requestSentTime: LocalTime.now())
            offset += newChunkLength
            pieceLengthLeft -= newChunkLength
        }
        def awaiting = new PieceAwaiting(chunksCount, piece.pieceLength)
        processingPieces.put piece.index, awaiting

        sendRequestsToPeers peerManager.connectedPeers.findAll {
            dataAvailability.get(it)?.isPieceAvailable(piece.index)
        }, PEER_REQUEST_QUEUE_SIZE as int
        return awaiting.promise
    }

    def sendRequestsToPeers(Collection<Peer> peers, int size) {
        peers.each { peer ->
            List<ChunkRequest> requests = []
            (1..size).each {
                def req = getNextRequest()
                if (req != null) {
                    requests << req
                }
            }
            requests.each {
                peer.sendMessage(new RequestMessage(it.chunk.piece.index, it.chunk.offset, it.chunk.length))
            }
        }
    }

    private synchronized ChunkRequest getNextRequest() {
        def result = pendingRequests.poll()
        if (!result) {
            result = sentRequests.poll()
        }
        if(result) {
            sentRequests.add(result)
        }
        return result
    }


    @Override
    void onMessage(Peer peer, PeerMessage message) {
        if (message instanceof PieceMessage) {
            PieceMessage pieceMessage = message as PieceMessage
            def pa = processingPieces.get(pieceMessage.pieceIndex())
            if (pa == null) {
                LOGGER.debug("No pa for chunk with index ${pieceMessage.pieceIndex()}")
                return
            }
            if (pa.completeChunk(pieceMessage.offset(), pieceMessage.data())) {
                processingPieces.remove(pieceMessage.pieceIndex())
            }
            sendRequestsToPeers([peer], 1)

        }
        if (message instanceof UnchokeMessage) {
            sendRequestsToPeers([peer], PEER_REQUEST_QUEUE_SIZE)
        }
    }

    @Override
    Set<Class> getHandledPeerMessages() {
        return [PieceMessage, UnchokeMessage]
    }
}

class PieceAwaiting {
    final CompletableFuture<byte[]> promise = new CompletableFuture<>()
    final AtomicLong chunksCount
    final byte[] data

    public PieceAwaiting(long chunksCount, long size) {
        data = new byte[size]
        this.chunksCount = new AtomicLong(chunksCount)
    }

    def boolean completeChunk(int offset, byte[] data) {
        def chunksLeft = chunksCount.decrementAndGet()
        println "Received chunk, left: ${chunksLeft}"
        System.arraycopy(data, 0, this.data, offset, (int) data.length)
        if (chunksCount.get() == 0) {
            promise.complete(this.data)
        }
        return promise.done
    }
}

class ChunkRequest {
    DataChunk chunk
    LocalTime requestSentTime
}