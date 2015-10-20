package com.example

import java.security.MessageDigest

/**
 * Created by Yasha on 14.10.2015.
 */
class BencodeDecoder {

    def torrentFile = [:]

    def decode(File torrentFile) {
        def encodedText = torrentFile.getText("ASCII")

        if (encodedText[0] != 'd') {
            throw new IllegalArgumentException("Unknown format of torrent file")
        }
        def map, index
        (map, index) = getValue(encodedText, 0)
        return map
    }

    def unwrapString(String text, int index) {
        def indexOfColon = text.indexOf(":", index)
        int stringLength = text.substring(index, indexOfColon).toBigInteger()
        def stringStartIndex = indexOfColon + 1
        def resultString = text.substring(stringStartIndex, stringStartIndex + stringLength)
        return [resultString, stringStartIndex + stringLength]
    }

    def unwrapMap(String text, int index) {
        def result = [:]
        while (text[index] != 'e') {
            def key, value
            (key, index) = unwrapString(text, index)
            def valueStartIndex = index
            (value, index) = getValue(text, index)
            if (key == 'info') {
                def infoBytes = text.substring(valueStartIndex, index).bytes
                MessageDigest.getInstance('SHA1').digest(infoBytes)
            }
            result.put key, value
        }
        return [result, index + 1]
    }

    def unwrapList(String text, int index) {
        def list = []
        while (text[index] != 'e') {
            def value
            (value, index) = getValue(text, index)
            list << value
        }
        return [list, index + 1]
    }

    def unwrapInt(String text, int index) {
        def endIndex = text.indexOf('e', index)
        return [text.substring(index, endIndex).toBigInteger(), endIndex + 1]
    }

    def getValue(String text, int index) {
        if (text[index] == 'd') {
            return unwrapMap(text, ++index)
        } else if (text[index] == 'l') {
            return unwrapList(text, ++index)
        } else if (text[index] == 'i') {
            return unwrapInt(text, ++index)
        }
        return unwrapString(text, index)

    }
}

