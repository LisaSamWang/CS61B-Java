package enigma;

import static enigma.EnigmaException.*;

/** Represents a permutation of a range of integers starting at 0 corresponding
 *  to the characters of an alphabet.
 *  @author Lisa Sam Wang
 */
class Permutation {

    /** Set this Permutation to that specified by CYCLES, a string in the
     *  form "(cccc) (cc) ..." where the c's are characters in ALPHABET, which
     *  is interpreted as a permutation in cycle notation.  Characters in the
     *  alphabet that are not included in any cycle map to themselves.
     *  Whitespace is ignored. */
    Permutation(String cycles, Alphabet alphabet) {
        _alphabet = alphabet;
        String permute = cycles.trim();
        permute = permute.replace("(", "-");
        permute = permute.replace(")", "-");
        _cycles = permute.split("-");
    }

    /** Add the cycle c0->c1->...->cm->c0 to the permutation, where CYCLE is
     *  c0c1...cm. */
    private void addCycle(String cycle) {
        int prevlength = _cycles.length;
        String[] updatedcycle = new String[prevlength + 1];
        for (int i = 0; i < prevlength; i++) {
            updatedcycle[i] = _cycles[i];
        }
        updatedcycle[prevlength + 1] = cycle;
        _cycles = updatedcycle;
    }

    /** Return the value of P modulo the size of this permutation. */
    final int wrap(int p) {
        int r = p % size();
        if (r < 0) {
            r += size();
        }
        return r;
    }

    /** Returns the size of the alphabet I permute. */
    int size() {
        return _alphabet.size();
    }

    /** Return the result of applying this permutation to P modulo the
     *  alphabet size. */
    int permute(int p) {
        return _alphabet.toInt(permute(_alphabet.toChar(wrap(p))));
    }

    /** Return the result of applying the inverse of this permutation
     *  to  C modulo the alphabet size. */
    int invert(int c) {
        return _alphabet.toInt(invert(_alphabet.toChar(wrap(c))));
    }


    /** Return the result of applying this permutation to the index of P
     *  in ALPHABET, and converting the result to a character of ALPHABET. */
    char permute(char p) {
        char result;
        result = p;
        for (int i = 0; i < _cycles.length; i += 1) {
            if (_cycles[i].contains(String.valueOf(p))) {
                String thecycle = _cycles[i];
                int index = thecycle.indexOf(String.valueOf(p));
                if (index != thecycle.length() - 1) {
                    result = thecycle.charAt(index + 1);
                } else {
                    result = thecycle.charAt(0);
                }
            }
        }
        return result;
    }

    /** Return the result of applying the inverse of this permutation to C. */
    char invert(char c) {
        char result;
        result = c;
        if (_cycles == null) {
            return result;
        } else {
            for (int i = 0; i < _cycles.length; i += 1) {
                if (_cycles[i].contains(String.valueOf(c))) {
                    String thecycle = _cycles[i];
                    int index = thecycle.indexOf(String.valueOf(c));
                    if (index != 0) {
                        result = thecycle.charAt(index - 1);
                    } else {
                        result = thecycle.charAt(thecycle.length() - 1);
                    }
                }
            }
            return result;
        }

    }

    /** Return the alphabet used to initialize this Permutation. */
    Alphabet alphabet() {
        return _alphabet;
    }

    /** Return true iff this permutation is a derangement (i.e., a
     *  permutation for which no value maps to itself). */
    boolean derangement() {
        int numletters = 0;
        for (int i = 0; i < _cycles.length; i++) {
            numletters += _cycles[i].length();
        }
        if (numletters < _alphabet.size()) {
            return false;
        }
        return true;
    }

    /** Alphabet of this permutation. */
    private Alphabet _alphabet;

    /** Array of strings with each string representing one cycle mapping. */
    private String[] _cycles;
}
