package cf.docent.bittorrent.data

/**
 * Created by docent on 09.12.15.
 */
class PieceAvailability {

    private final byte[] availabilityData

    public PieceAvailability(byte[] availabilityData) {
        this.availabilityData = availabilityData
    }

    boolean isPieceAvailable(long index) {
        if (!availabilityData) {
            return false
        }
        doWithSectorAndMask index, {sector, mask -> (availabilityData[sector] & mask) > 0}

    }

    def markPieceAvailable(int index) {
        doWithSectorAndMask(index, {sector, mask-> availabilityData[sector] |= mask})
    }

    private static def doWithSectorAndMask(long index, Closure action) {
        int maskByteIndex = index / 8
        int maskByteOffset = index % 8
        int test = 1 << (7 - maskByteOffset)
        action(maskByteIndex, test)
    }
}
