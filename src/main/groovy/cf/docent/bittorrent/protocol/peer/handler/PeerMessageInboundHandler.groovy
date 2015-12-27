package cf.docent.bittorrent.protocol.peer.handler
import cf.docent.bittorrent.protocol.peer.PeerMessage
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter

import java.util.function.Consumer
/**
 * Created by docent on 01.12.15.
 */
class PeerMessageInboundHandler extends ChannelInboundHandlerAdapter {

    Consumer<PeerMessage> messageListener

    @Override
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        messageListener.accept(msg as PeerMessage)
    }
}
