package com.example

import org.apache.commons.codec.net.URLCodec

/**
 * Created by Yasha on 21.10.2015.
 */
class Util {

    static byte[] sub(byte[] array, int start, int end) {
        def resultArray = new byte[end - start]
        System.arraycopy(array, start, resultArray, 0, end - start)
        return resultArray
    }

    static def <T> T subAndTransform(byte[] array, int start, int end, Closure<T> transformer) {
        transformer sub(array, start, end)
    }

    static def appendProperty(String self, String key, byte[] value) {
        if (!self.isEmpty()) {
            self += "&"
        }
        self + "$key=${new String(new URLCodec().encode(value))}"
    }

    static def appendProperty(String self, String key, String value) {
        appendProperty self, key, value.bytes
    }

}
