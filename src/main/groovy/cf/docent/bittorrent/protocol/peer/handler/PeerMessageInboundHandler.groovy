package cf.docent.bittorrent.protocol.peer.handler

import cf.docent.bittorrent.protocol.peer.PeerMessage
import cf.docent.bittorrent.protocol.peer.PeerMessageListener
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
/**
 * Created by docent on 01.12.15.
 */
class PeerMessageInboundHandler extends ChannelInboundHandlerAdapter {

    PeerMessageListener messageListener

    @Override
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        messageListener.onMessage(msg as PeerMessage)
    }
}
