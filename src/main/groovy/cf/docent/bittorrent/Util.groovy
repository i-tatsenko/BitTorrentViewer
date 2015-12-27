package cf.docent.bittorrent

import com.squareup.okhttp.HttpUrl

/**
 * Created by Yasha on 21.10.2015.
 */
class Util {

    static Random random = new Random()
    static char[] dictionary = "qwertyuiopasdfghjklzxcvbnmQWERTYUIOPASDFGHJKLZXCVBNM1234567890".toCharArray()

    static def <T> T subAndTransform(byte[] array, int start, int end, Closure<T> transformer) {
        transformer sub(array, start, end)
    }

    static byte[] sub(byte[] array, int start, int end) {
        def resultArray = new byte[end - start]
        System.arraycopy(array, start, resultArray, 0, end - start)
        return resultArray
    }

    static HttpUrl.Builder announceToBuilder(def announce) {
        def uri = new URI(announce as String)
        def builder = new HttpUrl.Builder()
                .scheme(uri.scheme)
                .host(uri.host)
                .port(portFromUri(uri))
        uri.path.split(/\//).each { builder.addEncodedPathSegment(it) }
        uri.query?.split(/&/)?.each {
            def pair = it.split('=')
            builder.addEncodedQueryParameter(pair[0], pair[1])
        }
        builder
    }

    private static int portFromUri(URI uri) {
        if (uri.port > 0) {
            return uri.port
        }
        switch (uri.scheme) {
            case 'http': return 80
            case 'https': return 443
        }
        return uri.port
    }

    static def randomString(int length) {
        def chars = new char[length]
        (0..(length - 1)).each {
            chars[it] = dictionary[random.nextInt(dictionary.length)]
        }
        return new String(chars)
    }

    static def randomNums(int length) {
        def random = new Random()
        (0..<length).collect {random.nextInt(10)}.join("")
    }

    static def twoBytesToInt(byte[] b) {
        return ((b[0] & 0xFF) << 8) + (b[1] & 0xFF)
    }

}
