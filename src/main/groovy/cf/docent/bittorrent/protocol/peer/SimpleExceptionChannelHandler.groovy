package cf.docent.bittorrent.protocol.peer

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel

/**
 * Created by docent on 21.11.15.
 */
class SimpleExceptionChannelHandler extends ChannelInitializer<SocketChannel> {

    final Closure fatalExceptionHandler

    public SimpleExceptionChannelHandler(Closure onFatalException) {
        fatalExceptionHandler = onFatalException
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline().addLast(new ChannelHandler() {
            @Override
            void handlerAdded(ChannelHandlerContext ctx) throws Exception {

            }

            @Override
            void handlerRemoved(ChannelHandlerContext ctx) throws Exception {

            }

            @Override
            void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
                if (!ctx.channel().open) {
                    if (fatalExceptionHandler.parameterTypes.length > 0) {
                        fatalExceptionHandler(cause)
                    } else {
                        fatalExceptionHandler()
                    }
                }
            }
        })
    }
}
