package cf.docent.bittorrent.protocol.peer
import cf.docent.bittorrent.protocol.NetDestination
import cf.docent.bittorrent.protocol.peer.handler.PeerMessageDecoder
import cf.docent.bittorrent.protocol.peer.handler.PeerMessageInboundHandler
import cf.docent.bittorrent.protocol.peer.handler.PeerMessageOutboundHandler
import cf.docent.bittorrent.protocol.peer.handler.SimpleExceptionChannelHandler
import cf.docent.bittorrent.protocol.peer.message.PeerMessageFactory
import groovy.transform.PackageScope
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

@PackageScope
class PeerConnection implements PeerMessageListener {

    private static final Logger LOGGER = LogManager.getLogger(PeerConnection)
    private PeerMessageListener peerMessageListener

    final NetDestination destination
    volatile ConnectionStatus connectionStatus = ConnectionStatus.CONNECTING
    private PeerConnectionStatusListener connectionEventListener
    private NioEventLoopGroup eventGroup = new NioEventLoopGroup()
    private Channel channel

    private PeerConnection(NetDestination netDestination, PeerConnectionStatusListener connectionEventListener) {
        this.connectionEventListener = connectionEventListener
        this.destination = netDestination
    }

    public static PeerConnection connect(NetDestination netDestination, PeerConnectionStatusListener connectionEventListener, PeerMessageListener peerMessageListener) {
        def connection = new PeerConnection(netDestination, connectionEventListener)
        connection.peerMessageListener = peerMessageListener
        connection.connectToPeer()
        return connection
    }

    def sendToPeer(PeerMessage peerMessage) {
        channel.writeAndFlush(peerMessage)
    }

    private def connectToPeer() {
        def bootstrap = new Bootstrap()
        bootstrap.group(eventGroup)
        bootstrap.channel(NioSocketChannel)
        bootstrap.handler(new SimpleExceptionChannelHandler(this.&close))

        def channelFuture = bootstrap.connect(destination.address, destination.port)
        def pipeline = channelFuture.channel().pipeline()
        pipeline.addLast(new PeerMessageOutboundHandler(), new PeerMessageDecoder(new PeerMessageFactory()), new PeerMessageInboundHandler(messageListener: this))

        channelFuture.addListener { connectionFuture ->
            if (connectionFuture.success) {
                processSuccessfulConnection(channelFuture)
            } else {
                processFailedConnection(channelFuture.cause())
            }
        }
    }

    private def processSuccessfulConnection(ChannelFuture channelFuture) {
        channel = channelFuture.channel()
        changeConnectionStatus(ConnectionStatus.CONNECTED)
    }

    private def processFailedConnection(Throwable cause) {
        LOGGER.debug("Can't connect to $destination due to ${cause.message}")
        changeConnectionStatus(ConnectionStatus.CONNECTION_FAILED)
        closeSilently()
    }

    private def changeConnectionStatus(ConnectionStatus newStatus) {
        ConnectionStatus old = connectionStatus
        connectionStatus = newStatus
        connectionEventListener?.statusChanged(old, newStatus, this)
    }

    def close() {
        changeConnectionStatus(ConnectionStatus.DISCONNECTED)
        closeSilently()
    }

    def closeSilently() {
        channel?.close()
        eventGroup.shutdownGracefully()
    }

    @Override
    String toString() {
        return "Seed connection:{$destination status: $connectionStatus}"
    }

    @Override
    def onMessage(PeerMessage peerMessage) {
        peerMessageListener.onMessage(peerMessage)
    }
}
