package com.example.model


import static com.example.Util.subAndTransform

/**
 * Created by Yasha on 22.10.2015.
 */
class BString {
    static final int BYTES_IN_DESTINATION = 6
    byte[] bytes

    String toString(){
        new String(bytes)
    }

    def toNetDestinations() {
        if (bytes.length % BYTES_IN_DESTINATION != 0) {
            throw new IllegalArgumentException("Can't convert ${bytes.length} to $count destinations")
        }
        def result =[]
        (1..<(bytes.length / BYTES_IN_DESTINATION)).each {
            def offset = it * BYTES_IN_DESTINATION
            result << "${subAndTransform(bytes, offset, offset + 4, Inet4Address.&getByAddress).hostAddress}:${subAndTransform(bytes, offset + 4, offset + 6, this.&twoBytesToInt)}"
        }
        return result
    }

    def twoBytesToInt(byte[] b) {
        return ((b[0] & 0xFF) << 8) + (b[1] & 0xFF)
    }
}
