package enigma;

/** An alphabet of encodable characters.  Provides a mapping from characters
 *  to and from indices into the alphabet.
 *  @author Lisa Sam Wang
 */
class Alphabet {

    /** A new alphabet containing CHARS.  Character number #k has index
     *  K (numbering from 0). No character may be duplicated. */
    Alphabet(String chars) {
        _chars = chars;
        _charArray = new char[chars.length()];
        for (int i = 0; i < chars.length(); i++) {
            _charArray[i] = _chars.charAt(i);
        }
    }

    /** A default alphabet of all upper-case characters. */
    Alphabet() {
        this("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
    }

    /** Returns the size of the alphabet. */
    int size() {
        return _chars.length();
    }

    /** Returns true if preprocess(CH) is in this alphabet. */
    boolean contains(char ch) {
        return _chars.contains(String.valueOf(ch));
    }

    /** Returns character number INDEX in the alphabet, where
     *  0 <= INDEX < size(). */
    char toChar(int index) {

        if (index < 0 || index >= size()) {
            throw new EnigmaException("Character index not in correct range");
        }
        return _charArray[index];
    }

    /** Returns the index of character preprocess(CH), which must be in
     *  the alphabet. This is the inverse of toChar(). */
    int toInt(char ch) {
        boolean in = false;
        int index = -1;
        for (int i = 0; i < size(); i += 1) {
            if (_charArray[i] == ch) {
                in = true;
                index = i;
            }
        }
        if (!in) {
            throw new EnigmaException("Invalid character.");
        }
        return index;
    }
    /** String of characters.*/
    private String _chars;
    /** Array of characters in form of Strings.*/
    private char[] _charArray;
}
