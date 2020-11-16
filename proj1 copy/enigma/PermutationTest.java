package enigma;

import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.Timeout;
import static org.junit.Assert.*;

import static enigma.TestUtils.*;

/** The suite of all JUnit tests for the Permutation class.
 *  @author Lisa Sam Wang
 */
public class PermutationTest {

    /** Testing time limit. */
    @Rule
    public Timeout globalTimeout = Timeout.seconds(5);

    /* ***** TESTING UTILITIES ***** */

    private Permutation perm;
    private String alpha = UPPER_STRING;

    /** Check that perm has an alphabet whose size is that of
     *  FROMALPHA and TOALPHA and that maps each character of
     *  FROMALPHA to the corresponding character of FROMALPHA, and
     *  vice-versa. TESTID is used in error messages. */
    private void checkPerm(String testId,
                           String fromAlpha, String toAlpha) {
        int N = fromAlpha.length();
        assertEquals(testId + " (wrong length)", N, perm.size());
        for (int i = 0; i < N; i += 1) {
            char c = fromAlpha.charAt(i), e = toAlpha.charAt(i);
            assertEquals(msg(testId, "wrong translation of '%c'", c),
                         e, perm.permute(c));
            assertEquals(msg(testId, "wrong inverse of '%c'", e),
                         c, perm.invert(e));
            int ci = alpha.indexOf(c), ei = alpha.indexOf(e);
            assertEquals(msg(testId, "wrong translation of %d", ci),
                         ei, perm.permute(ci));
            assertEquals(msg(testId, "wrong inverse of %d", ei),
                         ci, perm.invert(ei));
        }
    }

    /* ***** TESTS ***** */

    @Test
    public void checkFalseDerangement() {
        perm = new Permutation("(AVOLDRWFIUQ) (BZKSMNHYC) (X)", UPPER);
        assertEquals(false, perm.derangement());
    }
    @Test
    public void checkTrueDerangement() {
        perm = new Permutation("(ALOVDRWFIUQ) (BZKSMHNYC) (EGJTPX)", UPPER);
        assertEquals(true, perm.derangement());
    }
    @Test
    public void checkPermuteInt() {
        perm = new Permutation("(AVOLDRWFIUX) (BZKSMNHYC) (EGTJPQ)", UPPER);
        assertEquals(25, perm.permute(1));
    }
    @Test
    public void checkInvertInt() {
        perm = new Permutation("(AVOLDRWFIUQ) (BZKSMNHYC) (EGTJPX)", UPPER);
        assertEquals(1, perm.invert(25));
    }
    @Test
    public void checkPermuteChar() {
        perm = new Permutation("(AVOLDRWFIUQ) (BZKSMNHYC) (EGTJPX)", UPPER);
        assertEquals('F', perm.permute('W'));
    }
    @Test
    public void checkInvertChar() {
        perm = new Permutation("(AVOLDRWFIUQ) (BZKSMNHYC) (EGTJPX)", UPPER);
        assertEquals('W', perm.invert('F'));
    }
    @Test
    public void checkPermuteCharEdge() {
        perm = new Permutation("(AVOLDRWFIUQ) (BZKSMNHYC) (EGTJPX)", UPPER);
        assertEquals('A', perm.permute('Q'));
    }
    @Test
    public void checkInvertCharEdge() {
        perm = new Permutation("(AVOLDRWFIUQ) (BZKSMNHYC) (EGTJPX)", UPPER);
        assertEquals('Q', perm.invert('A'));
    }
    @Test
    public void checkPermuteIntEdge() {
        perm = new Permutation("(AVOLDRWFIUQ) (BZKSMNHYC) (EGTJPX)", UPPER);
        assertEquals(0, perm.permute(16));
    }
    @Test
    public void checkInvertIntEdge() {
        perm = new Permutation("(AVOLDRWFIUQ) (BZKSMNHYC) (EGTJPX)", UPPER);
        assertEquals(16, perm.invert(0));
    }

}
