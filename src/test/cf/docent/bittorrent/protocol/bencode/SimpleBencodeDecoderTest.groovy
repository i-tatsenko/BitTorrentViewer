package cf.docent.bittorrent.protocol.bencode

import spock.lang.Specification
/**
 * Created by Yasha on 14.10.2015.
 */
class SimpleBencodeDecoderTest extends Specification {

    def decoder = new SimpleBencodeDecoder()

    def 'unwrap string'() {
        expect:
        decoder.unwrapString(text.bytes, 0) == [string, newIndex]

        where:
        text                     | string                | newIndex
        '5:hello'                | 'hello'               | 7
        '10:abcsdfqwer'          | 'abcsdfqwer'          | 13
        '19:HelloKittyMrsHarris' | 'HelloKittyMrsHarris' | 22
    }

    def unwrapInt() {
        expect:
        decoder.unwrapInt(text.bytes, 0) == [integer, index]

        where:
        text   | integer | index
        '123e' | 123     | 4
        '1e'   | 1       | 2
    }

    def unwrapList() {
        expect:
        decoder.unwrapList(text.bytes, 0) == [resultList, index]

        where:
        text                   | resultList                | index
        '5:hello5:worldi16ee'  | ['hello', 'world', 16]    | 19
        '5:hellol5:world1:aee' | ['hello', ['world', 'a']] | 20
    }

    def unwrapMap() {
        expect:
        decoder.unwrapMap(text.bytes, 0) == [resultMap, index]

        where:
        text              | resultMap                                  | index
        '5:hello5:worlde' | [hello: 'world']                           | 15
        '5:hello' + 'd' +
                '5:hello' +
                'l' +
                '5:world' + '1:!' +
                'e' +
                '3:int' + 'i123e' +
                'e' +
                'e'       | [hello: [hello: ['world', '!'], int: 123]] | 39
    }
}
