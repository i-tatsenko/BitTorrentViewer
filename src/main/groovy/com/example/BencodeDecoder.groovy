package com.example

import java.security.MessageDigest

import static com.example.Util.sub
import static com.example.Util.subAndTransform

/**
 * Created by Yasha on 14.10.2015.
 */
class BencodeDecoder {

    def decode(byte[] bytes) {
        if (bytes[0] != 'd'.bytes[0]) {
            throw new IllegalArgumentException("Unknown format of torrent file")
        }
        def map, index
        (map, index) = getValue(bytes, 0)
        return map
    }

    def decode(File torrentFile) {
        def encodedBytes = torrentFile.bytes
        decode(encodedBytes)
    }

    def unwrapString(byte[] bytes, int index) {
        def indexOfColon = bytes.findIndexOf(index, {it == ':'.bytes[0]})
        int stringLength = subAndTransform(bytes, index, indexOfColon, {new String(it as byte[]).toBigInteger()})
        def stringStartIndex = indexOfColon + 1
        def resultString = subAndTransform(bytes, stringStartIndex, stringStartIndex + stringLength, {new String(it)})
        return [resultString, stringStartIndex + stringLength]
    }

    def unwrapMap(byte[] text, int index) {
        def result = [:]
        while (text[index] != 'e'.bytes[0]) {
            def key, value
            (key, index) = unwrapString(text, index)
            def valueStartIndex = index
            (value, index) = getValue(text, index)
            if (key == 'info') {
                def infoBytes = sub(text, valueStartIndex, index)
                def digest = MessageDigest.getInstance('SHA1').digest(infoBytes)
                result << [info_hash: digest]
            }
            result.put key, value
        }
        return [result, index + 1]
    }

    def unwrapList(byte[] text, int index) {
        def list = []
        while (text[index] != 'e'.bytes[0]) {
            def value
            (value, index) = getValue(text, index)
            list << value
        }
        return [list, index + 1]
    }

    def unwrapInt(byte[] text, int index) {
        def endIndex = text.findIndexOf(index, {it == 'e'.bytes[0]})
        return [subAndTransform(text, index, endIndex, {new String(it).toBigInteger()}), endIndex + 1]
    }

    def getValue(byte[] bytes, int index) {
        if (bytes[index] == 'd'.bytes[0]) {
            return unwrapMap(bytes, ++index)
        } else if (bytes[index] == 'l'.bytes[0]) {
            return unwrapList(bytes, ++index)
        } else if (bytes[index] == 'i'.bytes[0]) {
            return unwrapInt(bytes, ++index)
        }
        return unwrapString(bytes, index)

    }
}

