package com.example

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
        uri.path.split(/\//).each { builder.addEncodedPathSegment(it) }
        uri.query?.split(/&/)?.each {
            def pair = it.split('=')
            builder.addEncodedQueryParameter(pair[0], pair[1])
        }
        builder
    }

    static def randomString(int length) {
        def chars = new char[length]
        (0..(length - 1)).each {
            chars[it] = dictionary[random.nextInt(dictionary.length)]
        }
        return new String(chars)
    }

}
