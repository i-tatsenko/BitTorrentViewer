package cf.docent.bittorrent.protocol.bencode

import java.security.MessageDigest

import static cf.docent.bittorrent.Util.sub
import static cf.docent.bittorrent.Util.subAndTransform

/**
 * Created by Yasha on 14.10.2015.
 */
class SimpleBencodeDecoder implements BencodeDecoder {


    public static final byte END_BYTE = 'e'.bytes[0]
    public static final byte DICTIONARY_START_BYTE = 'd'.bytes[0]
    public static final byte LIST_START_BYTE = 'l'.bytes[0]
    public static final byte INTEGER_START_BYTE = 'i'.bytes[0]

    def decode(byte[] bytes) {
        if (bytes[0] != DICTIONARY_START_BYTE) {
            throw new IllegalArgumentException("Unknown format of torrent file")
        }
        def map, index
        (map, index) = getValue(bytes, 0)
        return map
    }

    def unwrapString(byte[] bytes, int index) {
        def result, i
        (result, i) = unwrapBString(bytes, index)
        [result.toString(), i]
    }

    def unwrapBString(byte[] bytes, int index) {
        def indexOfColon = bytes.findIndexOf(index, {it == ':'.bytes[0]})
        int stringLength = subAndTransform(bytes, index, indexOfColon, {new String(it as byte[]).toBigInteger()})
        def stringStartIndex = indexOfColon + 1
        def resultString = subAndTransform(bytes, stringStartIndex, stringStartIndex + stringLength, {new BString(bytes: it)})
        return [resultString, stringStartIndex + stringLength]
    }

    def unwrapMap(byte[] text, int index) {
        def result = [:]
        while (text[index] != END_BYTE) {
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
        while (text[index] != END_BYTE) {
            def value
            (value, index) = getValue(text, index)
            list << value
        }
        return [list, index + 1]
    }

    def unwrapInt(byte[] text, int index) {
        def endIndex = text.findIndexOf(index, {it == END_BYTE })
        return [subAndTransform(text, index, endIndex, {new String(it).toBigInteger()}), endIndex + 1]
    }

    def getValue(byte[] bytes, int index) {
        if (bytes[index] == DICTIONARY_START_BYTE) {
            return unwrapMap(bytes, ++index)
        } else if (bytes[index] == LIST_START_BYTE) {
            return unwrapList(bytes, ++index)
        } else if (bytes[index] == INTEGER_START_BYTE) {
            return unwrapInt(bytes, ++index)
        }
        return unwrapBString(bytes, index)

    }
}

