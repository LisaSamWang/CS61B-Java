package tablut;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import static tablut.Move.ROOK_MOVES;
import static tablut.Piece.*;
import static tablut.Square.*;


/** The state of a Tablut Game.
 *  @author Lisa Sam Wang
 */
@SuppressWarnings("unchecked")
class Board {

    /** The number of squares on a side of the board. */
    static final int SIZE = 9;

    /** The throne (or castle) square and its four surrounding squares.. */
    static final Square THRONE = sq(4, 4),
        NTHRONE = sq(4, 5),
        STHRONE = sq(4, 3),
        WTHRONE = sq(3, 4),
        ETHRONE = sq(5, 4);

    /** Initial positions of attackers. */
    static final Square[] INITIAL_ATTACKERS = {
        sq(0, 3), sq(0, 4), sq(0, 5), sq(1, 4),
        sq(8, 3), sq(8, 4), sq(8, 5), sq(7, 4),
        sq(3, 0), sq(4, 0), sq(5, 0), sq(4, 1),
        sq(3, 8), sq(4, 8), sq(5, 8), sq(4, 7)
    };

    /** Initial positions of defenders of the king. */
    static final Square[] INITIAL_DEFENDERS = {
        NTHRONE, ETHRONE, STHRONE, WTHRONE,
        sq(4, 6), sq(4, 2), sq(2, 4), sq(6, 4)
    };

    /** Initializes a game board with SIZE squares on a side in the
     *  initial position. */
    Board() {
        init();
    }

    /** Initializes a copy of MODEL. */
    Board(Board model) {
        copy(model);
    }

    /** Copies MODEL into me. */
    void copy(Board model) {
        if (model == this) {
            return;
        }
        init();
        this.psnWhite = (Stack<Square>) model.psnWhite.clone();
        this.psnBlack = (Stack<Square>) model.psnBlack.clone();
        this.pastboards = (ArrayList<Board>) model.pastboards.clone();
        this.empWhite = (Stack<Square>) model.empWhite.clone();
        this.empBlack = (Stack<Square>) model.empBlack.clone();
        this._repeated = model._repeated;
        this._winner = model._winner;
        this._moveCount = model._moveCount;
        this._moveLim = model._moveLim;
        this._turn = model._turn;
        this.pieceLocs = (HashMap<Piece, Object>) model.pieceLocs.clone();
        this.sqWhite = (ArrayList<Square>) model.sqWhite.clone();
        this.sqBlack = (ArrayList<Square>) model.sqBlack.clone();
        this.numcaps = (Stack<Integer>) model.numcaps.clone();
        for (int i = 0; i < SIZE; i++) {
            for (int k = 0; k < SIZE; k++) {
                Piece modelPiece = model.get(sq(i, k));
                if (this.get(sq(i, k)) != modelPiece) {
                    this.put(modelPiece, sq(i, k));
                }
            }
        }
    }

    /** Clears the board to the initial position. */
    void init() {
        _board = new Piece[9][9];
        clearUndo();
        _moveCount = 0;
        _turn = BLACK;
        _winner = null;
        _moveLim = Integer.MAX_VALUE;
        _repeated = false;
        for (int i = 0; i < SIZE; i++) {
            for (int k = 0; k < SIZE; k++) {
                put(EMPTY, sq(i, k));
            }
        }
        for (Square sq: INITIAL_DEFENDERS) {
            put(WHITE, sq);
        }
        for (Square sq: INITIAL_ATTACKERS) {
            put(BLACK, sq);
        }
        put(KING, THRONE);
        pieceLocs.put(KING, THRONE);
        pieceLocs.put(WHITE, sqWhite);
        pieceLocs.put(BLACK, sqBlack);
        clearUndo();
    }

    /** Set the move limit to N.  It is an error if 2*LIM <= moveCount(). */
    void setMoveLimit(int n) {
        if (2 * n <= moveCount()) {
            throw new IllegalArgumentException("Too many moves!");
        } else {
            _moveLim = n;
        }
    }

    /** Return a Piece representing whose move it is (WHITE or BLACK). */
    Piece turn() {
        return _turn;
    }

    /** Return the winner in the current position, or null if there is no winner
     *  yet. */
    Piece winner() {
        return _winner;
    }

    /** Returns true iff this is a win due to a repeated position. */
    boolean repeatedPosition() {
        return _repeated;
    }

    /** Record current position and set winner() next mover if the current
     *  position is a repeat. */
    private void checkRepeated() {
        for (Board past : pastboards) {
            if (past.sqBlack.containsAll(this.sqBlack)
                    && past.sqWhite.containsAll(this.sqWhite)
                    && past.kingPosition() == this.kingPosition()
                    && past.turn() == this.turn()) {
                _repeated = true;
                _winner = turn().opponent();
                break;
            }
        }
    }

    /** Return the number of moves since the initial position that have not been
     *  undone. */
    int moveCount() {
        return _moveCount;
    }

    /** Return location of the king. */
    Square kingPosition() {
        return (Square) pieceLocs.get(KING);
    }

    /** Return the contents the square at S. */
    final Piece get(Square s) {
        return get(s.col(), s.row());
    }

    /** Return the contents of the square at (COL, ROW), where
     *  0 <= COL, ROW <= 9. */
    final Piece get(int col, int row) {
        return _board[col][row];
    }

    /** Return the contents of the square at COL ROW. */
    final Piece get(char col, char row) {
        return get(col - 'a', row - '1');
    }

    /** Set square S to P. */
    final void put(Piece p, Square s) {
        if (p == EMPTY) {
            if (get(s) == WHITE) {
                sqWhite.remove(s);
            } else if (get(s) == BLACK) {
                sqBlack.remove(s);
            } else if (get(s) == KING
                    && s == kingPosition()) {
                pieceLocs.replace(KING, null);
            }
        }
        _board[s.col()][s.row()] = p;
        if (p == WHITE) {
            if (!sqWhite.contains(s)) {
                sqWhite.add(s);
            }
        } else if (p == KING) {
            pieceLocs.replace(KING, s);
        } else if (p == BLACK) {
            if (!sqBlack.contains(s)) {
                sqBlack.add(s);
            }
        }
    }

    /** Set square S to P and record for undoing. */
    final void revPut(Piece p, Square s) {
        if (p == EMPTY) {
            if (get(s) == KING || get(s) == WHITE) {
                empWhite.push(s);
            } else if (get(s) == BLACK) {
                empBlack.push(s);
            }
        }
        put(p, s);
        if (p == WHITE || p == KING) {
            psnWhite.push(s);
        }
        if (p == BLACK) {
            psnBlack.push(s);
        }
    }

    /** Set square COL ROW to P. */
    final void put(Piece p, char col, char row) {
        put(p, sq(col - 'a', row - '1'));
    }

    /** Return true iff FROM - TO is an unblocked rook move on the current
     *  board.  For this to be true, FROM-TO must be a rook move and the
     *  squares along it, other than FROM, must be empty. */
    boolean isUnblockedMove(Square from, Square to) {
        if (from.isRookMove(to)) {
            if (from.direction(to) == 0 || from.direction(to) == 2) {
                if (from.row() > to.row()) {
                    for (int check = from.row() - 1;
                         check >= to.row(); check--) {
                        if (get(sq(from.col(), check))
                                != EMPTY) {
                            return false;
                        }
                    }
                } else {
                    for (int check = from.row() + 1;
                         check <= to.row(); check++) {
                        if (get(sq(from.col(), check)) != EMPTY) {
                            return false;
                        }
                    }
                }
            } else if (from.direction(to) == 1
                    || from.direction(to) == 3) {
                if (from.col() > to.col()) {
                    for (int check = from.col() - 1;
                         check >= to.col(); check--) {
                        if (get(sq(check, from.row())) != EMPTY) {
                            return false;
                        }
                    }
                } else {
                    for (int check = from.col() + 1;
                         check <= to.col(); check++) {
                        if (get(sq(check, from.row())) != EMPTY) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    /** Return true iff FROM is a valid starting square for a move. */
    boolean isLegal(Square from) {
        return get(from).side() == _turn;
    }

    /** Return true iff FROM-TO is a valid move. */
    boolean isLegal(Square from, Square to) {
        if (isLegal(from) && isUnblockedMove(from, to)) {
            if (get(from) != KING && to == THRONE) {
                return false;
            }
            return true;
        }
        return false;
    }

    /** Return true iff MOVE is a legal move in the current
     *  position. */
    boolean isLegal(Move move) {
        return isLegal(move.from(), move.to());
    }

    /** Move FROM-TO, assuming this is a legal move. */
    void makeMove(Square from, Square to) {
        if (isLegal(from, to)) {
            Piece tomove = get(from);
            revPut(tomove, to);
            revPut(EMPTY, from);
            _moveCount = _moveCount + 1;
            checkRepeated();
            if (kingPosition() != null
                    && kingPosition().isEdge()) {
                _winner = WHITE;
            }
            if (winner() == null) {
                checkCaptures(to);
            }
            if (kingPosition() == null) {
                _winner = BLACK;
            }
            if (_moveCount >= _moveLim && winner() == null) {
                _winner = turn().opponent();
            }
            Board tostore = new Board(this);
            pastboards.add(tostore);
            _turn = _turn.opponent();
        } else {
            if (!hasMove(BLACK)) {
                _winner = WHITE;
            }
            if (!hasMove(WHITE)) {
                _winner = BLACK;
            }
        }
    }

    /** Helper function taking in Square TOCHECK,
     * see if it is surrounded by hostile squares.
     * @return true if piece should be captured. */
    boolean isSurrounded(Square tocheck) {
        Piece checkthis = get(tocheck);
        Piece myopp = checkthis.opponent();
        Square north =
                tocheck.rookMove(0, 1);
        Square east =
                tocheck.rookMove(1, 1);
        Square south =
                tocheck.rookMove(3, 1);
        Square west =
                tocheck.rookMove(2, 1);
        Piece pnorth = get(north);
        Piece peast = get(east);
        Piece psouth = get(south);
        Piece pwest = get(west);
        if ((north == THRONE || pnorth == myopp)
                && (east == THRONE || peast == myopp)
                && (south == THRONE || psouth == myopp)
                && (west == THRONE || pwest == myopp)) {
            return true;
        }
        return false;
    }

    /** Checks for captures from square TO and makes them. */
    void checkCaptures(Square to) {
        int numCaps = 0;
        for (int dir = 0; dir <= 3; dir++) {
            Square adj = to.rookMove(dir, 1);
            if (adj != null
                    && get(adj).side()
                    == turn().opponent()) {
                if (get(adj) == KING) {
                    if (kingPosition() == THRONE
                            || kingPosition() == NTHRONE
                            || kingPosition() == STHRONE
                            || kingPosition() == WTHRONE
                            || kingPosition() == ETHRONE) {
                        if (isSurrounded(adj)) {
                            capture(to, to.rookMove(dir, 2));
                            numCaps += 1;
                        }
                    } else {
                        Square otherside = to.rookMove(dir, 2);
                        if (otherside != null && get(to).side()
                                == get(otherside).side()) {
                            capture(to, to.rookMove(dir, 2));
                            numCaps += 1;
                        }
                    }
                } else {
                    Square otherside = to.rookMove(dir, 2);
                    if ((otherside != null && get(to).side()
                            == get(otherside).side())
                            || otherside == THRONE) {
                        if (get(to.rookMove(dir, 1)) == WHITE
                                && otherside == THRONE
                                && kingPosition() == THRONE) {
                            break;
                        } else {
                            capture(to, to.rookMove(dir, 2));
                            numCaps += 1;
                        }
                    }
                }
            }
        }
        numcaps.push(numCaps);
    }

    /** Move according to MOVE, assuming it is a legal move. */
    void makeMove(Move move) {
        makeMove(move.from(), move.to());
    }

    /** Capture the piece between SQ0 and SQ2, assuming a piece just moved to
     *  SQ0 and the necessary conditions are satisfied. */
    private void capture(Square sq0, Square sq2) {
        Piece captured = get(sq0.between(sq2));
        if (captured == KING) {
            revPut(EMPTY, kingPosition());
        } else {
            revPut(EMPTY, sq0.between(sq2));
        }
    }

    /** Undo one move.  Has no effect on the initial board. */
    void undo() {
        _turn = turn().opponent();
        if (_moveCount > 0) {
            if (turn() == WHITE) {
                if (!numcaps.isEmpty() && numcaps.peek() > 0) {
                    int C = numcaps.pop();
                    while (C > 0) {
                        put(BLACK, empBlack.peek());
                        undoPosition(true);
                        C -= 1;
                    }
                } else {
                    if (!psnWhite.isEmpty() && !empWhite.isEmpty()) {
                        Piece toundo = get(psnWhite.peek());
                        put(EMPTY, psnWhite.peek());
                        put(toundo, empWhite.peek());
                        undoPosition(false);
                    }
                }
            } else if (turn() == BLACK) {
                if (!numcaps.isEmpty() && numcaps.peek() > 0) {
                    int C = numcaps.pop();
                    if (kingPosition() == null) {
                        put(KING, empWhite.peek());
                        undoPosition(true);
                        C -= 1;
                    }
                    while (C > 0) {
                        put(WHITE, empWhite.peek());
                        undoPosition(true);
                        C -= 1;
                    }
                } else {
                    if (!psnBlack.isEmpty() && !empBlack.isEmpty()) {
                        put(EMPTY, psnBlack.peek());
                        put(BLACK, empBlack.peek());
                        undoPosition(false);
                    }
                }
            }
            _moveCount = _moveCount - 1;
        }
    }

    /** Remove record of current position, unless it is repeated,
     *  or we are at the first move, taking in DIDC. */
    private void undoPosition(boolean didC) {
        if (!_repeated || moveCount() > 0) {
            if (turn() == WHITE) {
                if (!didC) {
                    psnWhite.pop();
                    empWhite.pop();
                } else {
                    empBlack.pop();
                }
            } else if (turn() == BLACK) {
                if (!didC) {
                    psnBlack.pop();
                    empBlack.pop();
                } else {
                    empWhite.pop();
                }
            }
        }
        _repeated = false;
    }

    /** Clear the undo stack and board-position counts. Does not modify the
     *  current position or win status. */
    void clearUndo() {
        pastboards.clear();
        empBlack.clear();
        psnBlack.clear();
        empWhite.clear();
        psnWhite.clear();
        numcaps.clear();
    }

    /** Return a new mutable list of all legal moves on the current board for
     *  SIDE (ignoring whose turn it is at the moment). */
    List<Move> legalMoves(Piece side) {
        List<Move> possMoves = new ArrayList<Move>();
        for (Square sq : pieceLocations(side)) {
            for (int dir = 0; dir < 4; dir++) {
                if (sq != null) {
                    Move.MoveList tocheck = ROOK_MOVES[sq.index()][dir];
                    for (Move each : tocheck) {
                        boolean checkleg = isLegal(each);
                        if (checkleg) {
                            possMoves.add(each);
                        }
                    }
                }
            }
        }
        return possMoves;
    }

    /** Return true iff SIDE has a legal move. */
    boolean hasMove(Piece side) {
        if (legalMoves(side) != null) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return toString(true);
    }

    /** Return a text representation of this Board.  If COORDINATES, then row
     *  and column designations are included along the left and bottom sides.
     */
    String toString(boolean coordinates) {
        Formatter out = new Formatter();
        for (int r = SIZE - 1; r >= 0; r -= 1) {
            if (coordinates) {
                out.format("%2d", r + 1);
            } else {
                out.format("  ");
            }
            for (int c = 0; c < SIZE; c += 1) {
                out.format(" %s", get(c, r));
            }
            out.format("%n");
        }
        if (coordinates) {
            out.format("  ");
            for (char c = 'a'; c <= 'i'; c += 1) {
                out.format(" %c", c);
            }
            out.format("%n");
        }
        return out.toString();
    }

    /** Return the locations of all pieces on SIDE. */
    public HashSet<Square> pieceLocations(Piece side) {
        assert side != EMPTY;
        HashSet<Square> allLocsonSide = new HashSet<Square>();
        if (side == WHITE || side == KING) {
            allLocsonSide.addAll(sqWhite);
            allLocsonSide.add((Square) pieceLocs.get(KING));
        } else {
            allLocsonSide.addAll(sqBlack);
        }
        return allLocsonSide;
    }

    /** Return the contents of _board in the order of SQUARE_LIST as a sequence
     *  of characters: the toString values of the current turn and Pieces. */
    String encodedBoard() {
        char[] result = new char[Square.SQUARE_LIST.size() + 1];
        result[0] = turn().toString().charAt(0);
        for (Square sq : SQUARE_LIST) {
            result[sq.index() + 1] = get(sq).toString().charAt(0);
        }
        return new String(result);
    }

    /** Manually sets the turn for SIDE. */
    void setTurn(Piece side) {
        _turn = side;
    }

    /** Piece whose turn it is (WHITE or BLACK). */
    private Piece _turn;
    /** Cached value of winner on this board, or null if it has not been
     *  computed. */
    private Piece _winner;
    /** Number of (still undone) moves since initial position. */
    private int _moveCount;
    /** True when current board is a repeated position (ending the game). */
    private boolean _repeated;
    /** Current board. */
    private Piece[][] _board;
    /** The move limit. */
    private int _moveLim = Integer.MAX_VALUE;
    /** List of squares for BLACK. */
    private ArrayList<Square> sqBlack = new ArrayList<Square>();
    /** List of squares for WHITE. */
    private ArrayList<Square> sqWhite = new ArrayList<Square>();
    /** Hashmap with the types of pieces mapped to locations. */
    private HashMap<Piece, Object> pieceLocs = new HashMap<Piece, Object>();
    /** List of past board positions. */
    private ArrayList<Board> pastboards = new ArrayList<Board>();
    /** A stack recording moves that were played by white. */
    private Stack<Square> psnWhite = new Stack<Square>();
    /** A stack recording empties left by white undo moves. */
    private Stack<Square> empWhite = new Stack<Square>();
    /** A stack recording empties left by black undo moves. */
    private Stack<Square> empBlack = new Stack<Square>();
    /** A stack recording moves that were played by black. */
    private Stack<Square> psnBlack = new Stack<Square>();
    /** Stack of how many captures happened each move. */
    private Stack<Integer> numcaps = new Stack<Integer>();

}
