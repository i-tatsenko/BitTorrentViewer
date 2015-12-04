package cf.docent.bittorrent.protocol.peer.handler

import cf.docent.bittorrent.protocol.peer.PeerConnection
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

/**
 * Created by docent on 22.11.15.
 */
class LoggingInboundHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LogManager.getLogger()

    private PeerConnection seedConnection

    public LoggingInboundHandler(PeerConnection seedConnection) {
        this.seedConnection = seedConnection
    }

    @Override
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        def buf = (ByteBuf) msg
        def readableBytes = buf.readableBytes()
        def bytes = new byte[readableBytes]
        buf.readBytes(bytes)
        LOGGER.debug("Received ${new String(bytes)} from ${seedConnection.destination}")
        buf.release()
    }
}
