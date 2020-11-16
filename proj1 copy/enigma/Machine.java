package enigma;

import java.util.ArrayList;
import java.util.Collection;

import static enigma.EnigmaException.*;

/** Class that represents a complete enigma machine.
 *  @author Lisa Sam Wang
 */
class Machine {

    /** A new Enigma machine with alphabet ALPHA, 1 < NUMROTORS rotor slots,
     *  and 0 <= PAWLS < NUMROTORS pawls.  ALLROTORS contains all the
     *  available rotors. */
    Machine(Alphabet alpha, int numRotors, int pawls,
            Collection<Rotor> allRotors) {
        _alphabet = alpha;
        _rotorslots = numRotors;
        _pawls = pawls;
        _allrotors = new ArrayList<Rotor>(allRotors);
        _myrotors = new Rotor[numRotors];
    }

    /** Return the number of rotor slots I have. */
    int numRotors() {
        return _rotorslots;
    }

    /** Return the number pawls (and thus rotating rotors) I have. */
    int numPawls() {
        return _pawls;
    }

    /** Return my Array of rotors. */
    Rotor[] myRotors() {
        return _myrotors; }

    /** Set my rotor slots to the rotors named ROTORS from my set of
     *  available rotors (ROTORS[0] names the reflector).
     *  Initially, all rotors are set at their 0 setting. */
    void insertRotors(String[] rotors) {
        for (int i = 0; i < rotors.length; i++) {
            for (int k = 0; k < _allrotors.size(); k++) {
                String wantedrotor = rotors[i];
                String checkrotor = _allrotors.get(k).name();
                if (wantedrotor.equals(checkrotor)) {
                    _myrotors[i] = _allrotors.get(k);
                }
            }
        }
        int actpawls = 0;
        for (int i = 0; i < _myrotors.length; i++) {
            if (_myrotors[i] == null) {
                throw new EnigmaException("Bad rotor name");
            }
            if (_myrotors[i].rotates()) {
                actpawls++;
            }
        }
        if (actpawls != numPawls()) {
            throw new EnigmaException("Wrong number of arguments in setting");
        }
        if (_myrotors.length != rotors.length) {
            throw new EnigmaException("Nonexistent rotor");
        }
    }

    /** Set my rotors according to SETTING, which must be a string of
     *  numRotors()-1 characters in my alphabet. The first letter refers
     *  to the leftmost rotor setting (not counting the reflector).  */
    void setRotors(String setting) {
        String[] withring = setting.split(" ");
        if (withring[0].length() != numRotors() - 1) {
            throw new EnigmaException("Wrong length of string");
        }
        if (withring.length == 2) {
            for (int i = 0; i < withring[1].length(); i++) {
                char mychar = withring[1].charAt(i);
                int intchar = _alphabet.toInt(mychar);
                int origcharint = _alphabet.toInt(withring[0].charAt(i));
                _myrotors[i + 1].set(
                        (intchar + origcharint) % _alphabet.size());
            }

        }
        for (int i = 0; i < withring[0].length(); i++) {
            char mychar = withring[0].charAt(i);
            if (!_alphabet.contains(mychar)) {
                throw new EnigmaException("Letter not in alphabet");
            }
            _myrotors[i + 1].set(mychar);
        }
    }

    /** Set the plugboard to PLUGBOARD. */
    void setPlugboard(Permutation plugboard) {
        _plugboard = plugboard;
    }

    /** Returns the result of converting the input character C (as an
     *  index in the range 0..alphabet size - 1), after first advancing
     *  the machine. */
    int convert(int c) {
        boolean stepped = true;
        boolean atnotch = _myrotors[_rotorslots - 1].atNotch();
        _myrotors[_rotorslots - 1].advance();
        for (int i = _rotorslots - 2;
             i >= _rotorslots - 1 - _pawls; i--) {
            if ((stepped && atnotch)
                || (stepped && _myrotors[i].atNotch()
                    && _myrotors[i - 1].rotates())) {
                stepped = true;
                atnotch = _myrotors[i].atNotch();
                _myrotors[i].advance();
            } else {
                stepped = false;
                atnotch = _myrotors[i].atNotch();
            }
        }
        int answer = _plugboard.permute(c);
        for (int i = _rotorslots - 1; i >= 0; i--) {
            answer = _myrotors[i].convertForward(answer);
        }
        for (int i = 1; i < _rotorslots; i++) {
            answer = _myrotors[i].convertBackward(answer);
        }
        answer = _plugboard.permute(answer);
        return answer;
    }

    /** Returns the encoding/decoding of MSG, updating the state of
     *  the rotors accordingly. */
    String convert(String msg) {
        msg = msg.replaceAll(" ", "");
        String result = "";
        for (int i = 0; i < msg.length(); i++) {
            char converted = _alphabet.toChar(
                    convert(_alphabet.toInt(msg.charAt(i))));
            result += converted;
        }
        return result;
    }

    /** Common alphabet of my rotors. */
    private final Alphabet _alphabet;

    /** Number of rotor slots. */
    private final int _rotorslots;

    /** Number of pawls. */
    private int _pawls;

    /** Arraylist of all available rotors. */
    private ArrayList<Rotor> _allrotors;

    /** Array of rotors of this machine. */
    private Rotor[] _myrotors;

    /** The original plugboard. */
    private Permutation _plugboard;
}
