package cf.docent.bittorrent.protocol.peer
import cf.docent.bittorrent.TorrentChunkListener
import cf.docent.bittorrent.protocol.NetDestination
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
/**
 * Created by docent on 17.11.15.
 */
class SeedConnection {

    private static final Logger LOGGER = LogManager.getLogger(SeedConnection)


    final NetDestination destination
    TorrentChunkListener torrentChunkListener
    volatile int partsCompleted
    volatile ConnectionStatus connectionStatus = ConnectionStatus.CONNECTING
    private PeerConnectionStatusListener connectionEventListener
    private NioEventLoopGroup eventGroup = new NioEventLoopGroup()
    private Channel channel

    private SeedConnection(NetDestination netDestination, TorrentChunkListener torrentChunkListener, PeerConnectionStatusListener connectionEventListener) {
        this.connectionEventListener = connectionEventListener
        this.torrentChunkListener = torrentChunkListener
        this.destination = netDestination
    }

    public static SeedConnection connect(NetDestination netDestination, TorrentChunkListener chunkListener, PeerConnectionStatusListener connectionEventListener) {
        def connection = new SeedConnection(netDestination, chunkListener, connectionEventListener)
        connection.connectToPeer()
        return connection
    }

    private def connectToPeer() {
        def bootstrap = new Bootstrap()
        bootstrap.group(eventGroup)
        bootstrap.channel(NioSocketChannel)
        bootstrap.handler(new SimpleExceptionChannelHandler(this.&close))
        def channelFuture = bootstrap.connect(destination.address, destination.port)
        channelFuture.addListener { connectionFuture ->
            if (connectionFuture.success) {
                processSuccessfulConnection(channelFuture)
            } else {
                processFailedConnection(channelFuture.cause())
            }
        }

    }

    private def processSuccessfulConnection(ChannelFuture channelFuture) {
        changeConnectionStatus(ConnectionStatus.CONNECTED)
        channel = channelFuture.channel()
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
}
