package cf.docent.bittorrent.protocol.peer.handler
import cf.docent.bittorrent.protocol.peer.message.HandShakeMessage
import cf.docent.bittorrent.protocol.peer.message.PeerMessageFactory
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.Marker
import org.apache.logging.log4j.MarkerManager

class PeerMessageDecoder extends ByteToMessageDecoder {

    private static final Logger LOGGER = LogManager.getLogger(PeerMessageDecoder)
    public static final MESSAGE_ID_BYTES_LENGTH = 1

    private volatile String threadName
    private volatile boolean handshakeReceived = false
    private PeerMessageFactory peerMessageFactory
    private volatile long nextMessageLength
    private volatile boolean waitingBytesForNextMessage = false

    private Marker marker;

    public PeerMessageDecoder(PeerMessageFactory peerMessageFactory) {
        this.peerMessageFactory = peerMessageFactory
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List out) throws Exception {
        checkThreadAccess()
        marker = new MarkerManager.Log4jMarker(ctx.pipeline().channel().remoteAddress().toString())

        if (!handshakeReceived) {
            expectHandshake(byteBuf)
        } else {
            processNextPeerMessage(ctx, byteBuf, out)
        }
    }

    def processNextPeerMessage(ChannelHandlerContext ctx, ByteBuf byteBuf, List out) {
        checkThreadAccess()
        LOGGER.trace(marker, "Waiting for next message: $waitingBytesForNextMessage")
        if (waitingBytesForNextMessage) {
            def messageWasRead = readNextMessage(byteBuf, out)
            if (!messageWasRead) {
                LOGGER.trace(marker, "Message was not read")
                return
            }
            LOGGER.trace(marker, "message was read")
            waitingBytesForNextMessage = false
            return processNextPeerMessage(ctx, byteBuf, out)
        }
        checkThreadAccess()
        long messageLength = readNextMessageLength(byteBuf)
        if (messageLength < 0) {
            LOGGER.trace(marker, "Not enough bytes to read next message length")
            return
        }
        nextMessageLength = messageLength
        waitingBytesForNextMessage = true
        processNextPeerMessage(ctx, byteBuf, out)
    }

    def readNextMessage(ByteBuf bb, List out) {
        def readableBytes = bb.readableBytes()
        if (readableBytes < nextMessageLength) {
            LOGGER.trace(marker, "Waiting for next message bytes. Got: $readableBytes need: $nextMessageLength")
            return false
        }
        if (nextMessageLength == 0L) {
            LOGGER.trace(marker, "Next message length = 0")
            return true
        }
        byte messageId = bb.readByte()
        LOGGER.trace(marker, "Next message id: $messageId")
        nextMessageLength -= MESSAGE_ID_BYTES_LENGTH
        byte[] messageBytes = new byte[nextMessageLength]
        try {
            if (nextMessageLength > 0) bb.readBytes(messageBytes)
            LOGGER.trace(marker, "Message bytes count : ${messageBytes.length}")
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

    long readNextMessageLength(ByteBuf bb) {
        if (bb.readableBytes() < 4) {
            return -1
        }
        long result = bb.readUnsignedInt()
        LOGGER.trace(marker, "Next message length: $result")
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
