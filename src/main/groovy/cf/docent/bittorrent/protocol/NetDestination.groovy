package cf.docent.bittorrent.protocol
import cf.docent.bittorrent.Util
import groovy.transform.EqualsAndHashCode

import static cf.docent.bittorrent.Util.subAndTransform

@EqualsAndHashCode
class NetDestination {

    public static final int BYTES_IN_DESTINATION = 6

    final InetAddress address
    final int port

    public NetDestination(InetAddress address, int port) {
        this.address = address
        this.port = port
    }

    public static NetDestination createNetDestination(byte[] destinationBytes) {
        if (destinationBytes.length != BYTES_IN_DESTINATION) {
            throw new IllegalArgumentException("Can't convert ${destinationBytes.length} bytes to destinations")
        }
        return newNetDestination(destinationBytes, 0)
    }

    public static List<NetDestination> createNetDestinationList(byte[] destinationBytes) {
        if (destinationBytes.length % BYTES_IN_DESTINATION != 0) {
            throw new IllegalArgumentException("Can't convert ${destinationBytes.length} bytes to net destinations")
        }

        def ipCount = destinationBytes.length / BYTES_IN_DESTINATION
        return (0..<ipCount).collect {
            newNetDestination(destinationBytes, it * BYTES_IN_DESTINATION)
        }
    }

    private static NetDestination newNetDestination(byte[] destBytes, int offset) {
        def ipAddress = subAndTransform(destBytes, offset, offset + 4, Inet4Address.&getByAddress)
        def port = subAndTransform(destBytes, offset + 4, offset + 6, Util.&twoBytesToInt)
        return new NetDestination(ipAddress, port)
    }

    @Override
    String toString() {
        return "/${address.hostAddress}:$port"
    }
}
