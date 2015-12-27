package cf.docent.bittorrent.data

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by docent on 09.12.15.
 */
class PieceAvailabilityTest {

    private PieceAvailability underTest

    @Before
    void setUp() {
        def pieces = new byte[2]
        pieces[0] = 0b01010101
        pieces[1] = 0b00001011
        underTest  = new PieceAvailability(pieces);
    }

    @Test
    public void testIsPieceAvailable() throws Exception {
        assertEquals(underTest.isPieceAvailable(0), false);
        assertEquals(underTest.isPieceAvailable(1), true);
        assertEquals(underTest.isPieceAvailable(2), false);

        assertEquals(underTest.isPieceAvailable(10), false);
        assertEquals(underTest.isPieceAvailable(12), true);
        assertEquals(underTest.isPieceAvailable(13), false);
    }

    @Test
    public void testMarkPieceAvailable() throws Exception {
        assertEquals(underTest.isPieceAvailable(13), false);
        underTest.markPieceAvailable(13)
        assertEquals(underTest.isPieceAvailable(13), true)
    }
}