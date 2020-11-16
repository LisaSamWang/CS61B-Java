package enigma;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static enigma.EnigmaException.*;

/** Enigma simulator.
 *  @author Lisa Sam Wang
 */
public final class Main {

    /** Process a sequence of encryptions and decryptions, as
     *  specified by ARGS, where 1 <= ARGS.length <= 3.
     *  ARGS[0] is the name of a configuration file.
     *  ARGS[1] is optional; when present, it names an input file
     *  containing messages.  Otherwise, input comes from the standard
     *  input.  ARGS[2] is optional; when present, it names an output
     *  file for processed messages.  Otherwise, output goes to the
     *  standard output. Exits normally if there are no errors in the input;
     *  otherwise with code 1. */
    public static void main(String... args) {
        try {
            new Main(args).process();
            return;
        } catch (EnigmaException excp) {
            System.err.printf("Error: %s%n", excp.getMessage());
        }
        System.exit(1);
    }

    /** Check ARGS and open the necessary files (see comment on main). */
    Main(String[] args) {
        if (args.length < 1 || args.length > 3) {
            throw error("Only 1, 2, or 3 command-line arguments allowed");
        }

        _config = getInput(args[0]);

        if (args.length > 1) {
            _input = getInput(args[1]);
        } else {
            _input = new Scanner(System.in);
        }

        if (args.length > 2) {
            _output = getOutput(args[2]);
        } else {
            _output = System.out;
        }
    }

    /** Return a Scanner reading from the file named INNAME. */
    private Scanner getInput(String inname) {
        try {
            return new Scanner(new File(inname));
        } catch (IOException excp) {
            throw error("could not open %s", inname);
        }
    }

    /** Return a PrintStream writing to the file named OUTNAME. */
    private PrintStream getOutput(String outname) {
        try {
            return new PrintStream(new File(outname));
        } catch (IOException excp) {
            throw error("could not open %s", outname);
        }
    }

    /** Configure an Enigma machine from the contents of configuration
     *  file _config and apply it to the messages in _input, sending the
     *  results to _output. */
    private void process() {
        Machine enigma = readConfig();
        String nextl = _input.nextLine();
        while (_input.hasNext()) {
            String sett = nextl;
            if (!sett.contains("*")) {
                throw new EnigmaException("Invalid setting");
            }
            if (sett.startsWith("*")) {
                setUp(enigma, sett);
            }
            nextl = (_input.nextLine());
            while (nextl.isEmpty()) {
                nextl = (_input.nextLine());
                _output.println();
            }
            while (!(nextl.contains("*"))) {
                String result = enigma.convert(nextl);
                if (nextl.isEmpty()) {
                    _output.println();
                } else {
                    printMessageLine(result);
                }
                if (!_input.hasNext()) {
                    nextl = "*";
                } else {
                    nextl = (_input.nextLine());
                }
            }
        }
        while (_input.hasNextLine()) {
            _output.println();
            _input.nextLine();
        }
        if (!_input.hasNextLine()) {
            if (!nextl.startsWith("*")) {
                throw new EnigmaException("You did not put a setting!");
            }
        }
    }

    /** Return an Enigma machine configured from the contents of configuration
     *  file _config. */
    private Machine readConfig() {
        try {
            String alphabet = _config.next();
            if (alphabet.contains("(")
                    || alphabet.contains(")") || alphabet.contains("*")) {
                throw new EnigmaException("Invalid configuration");
            }
            _alphabet = new Alphabet(alphabet);

            if (!_config.hasNextInt()) {
                throw new EnigmaException("Need numRotors config");
            }
            int numRotors = _config.nextInt();
            if (!_config.hasNextInt()) {
                throw new EnigmaException("Need pawls config");
            }
            int pawls = _config.nextInt();
            nextconfig = _config.next();
            while (_config.hasNext()) {
                name = nextconfig;
                notches = _config.next();
                _allRotors.add(readRotor());
            }
            return new Machine(_alphabet, numRotors, pawls, _allRotors);
        } catch (NoSuchElementException excp) {
            throw error("configuration file truncated");
        }
    }

    /** Return a rotor, reading its description from _config. */
    private Rotor readRotor() {
        try {
            permkeys = "";
            nextconfig = _config.next();
            while (nextconfig.contains("(") && _config.hasNext()) {
                permkeys = permkeys.concat(nextconfig + " ");
                nextconfig = _config.next();
            }
            if (!_config.hasNext()) {
                permkeys = permkeys.concat(nextconfig + " ");
            }
            if (notches.charAt(0) == 'M') {
                return new MovingRotor(
                        name, new Permutation(permkeys, _alphabet),
                        notches.substring(1));
            } else if (notches.charAt(0) == 'N') {
                return new FixedRotor(
                        name, new Permutation(permkeys, _alphabet));
            } else if (notches.charAt(0) == 'R') {
                return new Reflector(
                        name, new Permutation(permkeys, _alphabet));
            } else {
                throw new EnigmaException("Invalid rotor type!");
            }
        } catch (NoSuchElementException excp) {
            throw error("bad rotor description");
        }
    }

    /** Set M according to the specification given on SETTINGS,
     *  which must have the format specified in the assignment. */
    private void setUp(Machine M, String settings) {
        String[] S = settings.split(" ");
        if (S.length - 1 < M.numRotors()) {
            throw new EnigmaException("Wrong number of arguments in settings");
        }
        String[] R = new String[M.numRotors()];
        for (int i = 1; i < M.numRotors() + 1; i++) {
            R[i - 1] = S[i];
        }
        for (int i = 0; i < R.length - 1; i++) {
            for (int k = i + 1; k < R.length; k++) {
                if (R[i].equals(R[k])) {
                    throw new EnigmaException("You already used this rotor");
                }
            }
        }
        M.insertRotors(R);
        if (!M.myRotors()[0].reflecting()) {
            throw new EnigmaException("First rotor has to be a reflector");
        }
        if (S.length > M.numRotors() + 2
                && !S[M.numRotors() + 2].startsWith("(")) {
            M.setRotors(S[M.numRotors() + 1] + " " + S[M.numRotors() + 2]);
        }
        M.setRotors(S[M.numRotors() + 1]);
        if ((S.length > M.numRotors() + 2
                && S[M.numRotors() + 2].startsWith("("))
                || (S.length > M.numRotors() + 3
                && S[M.numRotors() + 2].startsWith("("))) {
            String steckered = "";
            for (int i = M.numRotors() + 2; i < S.length; i++) {
                steckered += S[i];
            }
            if (!steckered.isEmpty()) {
                M.setPlugboard(new Permutation(steckered, _alphabet));
            }
        } else {
            M.setPlugboard(new Permutation("", _alphabet));
        }
    }

    /** Print MSG in groups of five (except that the last group may
     *  have fewer letters). */
    private void printMessageLine(String msg) {
        for (int i = 0; i < msg.length(); i += 5) {
            if (msg.length() - i <= 5) {
                _output.println(msg.substring(i));
            } else {
                _output.print(msg.substring(i, i + 5) + " ");
            }
        }
    }

    /** Alphabet used in this machine. */
    private Alphabet _alphabet;

    /** Source of input messages. */
    private Scanner _input;

    /** Source of machine configuration. */
    private Scanner _config;

    /** File for encoded/decoded messages. */
    private PrintStream _output;

    /** Arraylist with all rotors. */
    private ArrayList<Rotor> _allRotors = new ArrayList<>();

    /** Next token of configuration. */
    private String nextconfig;

    /** Current rotor's name to be set. */
    private String name;

    /** Notches of current rotor and rotor type. */
    private String notches;

    /** String with the cycles. */
    private String permkeys;
}
