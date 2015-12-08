package cf.docent.bittorrent.protocol.peer.handler

import cf.docent.bittorrent.protocol.peer.PeerMessage
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class PeerMessageOutboundHandler extends ChannelOutboundHandlerAdapter {

    private static final Logger LOGGER = LogManager.getLogger(PeerMessageOutboundHandler)

    @Override
    void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (!(msg instanceof PeerMessage)) {
            throw new IllegalArgumentException("Can't send object of type ${msg.class} to peer")
        }
        ByteBuf byteBuf = ctx.alloc().buffer(msg.messageBytes.length)
        byteBuf.writeBytes(msg.serialize())
        ctx.writeAndFlush(byteBuf, promise)
        LOGGER.debug("Sending $msg to peer")
    }
}
