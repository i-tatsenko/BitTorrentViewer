package cf.docent.bittorrent.protocol.peer.handler
import cf.docent.bittorrent.protocol.peer.message.HandShakeMessage
import cf.docent.bittorrent.protocol.peer.message.PeerMessageFactory
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
/**
 * Created by docent on 01.12.15.
 */
class PeerMessageDecoder extends ByteToMessageDecoder {

    private static final Logger LOGGER = LogManager.getLogger(PeerMessageDecoder)
    public static final MESSAGE_ID_BYTES_LENGTH = 1

    private volatile String threadName
    private volatile boolean handshakeReceived = false
    private PeerMessageFactory peerMessageFactory
    private volatile long nextMessageLength
    private volatile boolean waitingBytesForNextMessage = false

    public PeerMessageDecoder(PeerMessageFactory peerMessageFactory) {
        this.peerMessageFactory = peerMessageFactory
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List out) throws Exception {
        checkThreadAccess()
        if (!handshakeReceived) {
            expectHandshake(byteBuf)
        } else {
            processNextPeerMessage(ctx, byteBuf, out)
        }
    }

    def processNextPeerMessage(ChannelHandlerContext ctx, ByteBuf byteBuf, List out) {
        if (waitingBytesForNextMessage) {
            def messageWasRead = readNextMessage(byteBuf, out)
            if (!messageWasRead) {
                return
            }
            waitingBytesForNextMessage = false
            processNextPeerMessage(ctx, byteBuf, out)
        }
        long messageLength = readNextMessageLength(byteBuf)
        if (messageLength < 0) {
            return
        }
        nextMessageLength = messageLength
        waitingBytesForNextMessage = true
        processNextPeerMessage(ctx, byteBuf, out)
    }

    def readNextMessage(ByteBuf bb, List out) {
        def readableBytes = bb.readableBytes()
        if (readableBytes < nextMessageLength) {
            LOGGER.debug("Waiting for next message bytes. Got: $readableBytes need: $nextMessageLength")
            return false
        }
        if (nextMessageLength == 0L) {
            return true
        }
        byte messageId = bb.readByte()
        nextMessageLength -= MESSAGE_ID_BYTES_LENGTH
        byte[] messageBytes = new byte[nextMessageLength]
        try {
            if (nextMessageLength > 0) bb.readBytes(messageBytes)
        } catch (IndexOutOfBoundsException ioobe) {
            LOGGER.error("Index out of bounds. Readable bytes: $readableBytes nextMessageLength: $nextMessageLength")
            throw ioobe
        }
        def message = peerMessageFactory.messageFromBytes(messageId, messageBytes)
        if (message != null) {
            out << message
        }
        return true
    }

    static long readNextMessageLength(ByteBuf bb) {
        if (bb.readableBytes() < 4) {
            return -1
        }
        long result = bb.readUnsignedInt()
        if (result == 0L) {
            LOGGER.debug("Got 0 length message")
        }
        if (result > 100_000) {
            def bytes = new byte[100]
            bb.readBytes(bytes)
            LOGGER.debug(bytes.encodeHex())
        }
        return result
    }

    def expectHandshake(ByteBuf bb) {
        def handshakeMessageLength = HandShakeMessage.messageLength()
        if (bb.readableBytes() < handshakeMessageLength ) {
            return
        }
        byte[] bytes = new byte[handshakeMessageLength]
        bb.readBytes(bytes)
        def isAppropriateHandshakeMessage = HandShakeMessage.isHandshakeMessage(bytes)
        if (!isAppropriateHandshakeMessage) {
            LOGGER.error("Unknown format of handshake message ${bytes.encodeHex()}")
            throw new IllegalArgumentException("Unknown format of handshake message")
        }
        handshakeReceived = true
    }

    def checkThreadAccess() {
        if (!threadName) {
            setThreadName()
        }
        if (threadName != Thread.currentThread().name) {
            throw new IllegalArgumentException("Multiple threads accesses to one decoder")
        }
    }

    synchronized setThreadName() {
        threadName = Thread.currentThread().name
    }
}
