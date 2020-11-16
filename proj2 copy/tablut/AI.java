package tablut;


import java.util.ArrayList;

import static java.lang.Math.*;

import static tablut.Board.*;
import static tablut.Square.sq;
import static tablut.Piece.*;

/** A Player that automatically generates moves.
 *  @author Lisa Sam Wang
 */
class AI extends Player {

    /** A position-score magnitude indicating a win (for white if positive,
     *  black if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 20;
    /** A position-score magnitude indicating a forced win in a subsequent
     *  move.  This differs from WINNING_VALUE to avoid putting off wins. */
    private static final int WILL_WIN_VALUE = Integer.MAX_VALUE - 40;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI with no piece or controller (intended to produce
     *  a template). */
    AI() {
        this(null, null);
    }

    /** A new AI playing PIECE under control of CONTROLLER. */
    AI(Piece piece, Controller controller) {
        super(piece, controller);
    }

    @Override
    Player create(Piece piece, Controller controller) {
        return new AI(piece, controller);
    }

    @Override
    String myMove() {
        if (board().turn() == myPiece()
                && board().winner() == null) {
            Move mymove = findMove();
            System.out.println("* " + mymove);
            return mymove.toString();
        }
        return null;
    }

    @Override
    boolean isManual() {
        return false;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        _lastFoundMove = null;
        int nummoves = board().moveCount();
        if (board().hasMove(myPiece())) {
            if (board().turn() == WHITE) {
                if (nummoves <= CHECK1) {
                    findMove(board(), 1, true, 1, -INFTY, INFTY);
                } else if (nummoves <= CHECK2) {
                    findMove(board(), 2, true, 1, -INFTY, INFTY);
                } else {
                    findMove(board(), maxDepth(board()),
                            true, 1, -INFTY, INFTY);
                }
            } else if (board().turn() == BLACK) {
                if (nummoves <= CHECK1) {
                    findMove(board(), 1, true, -1, -INFTY, INFTY);
                } else if (nummoves <= CHECK2) {
                    findMove(board(), 2, true, -1, -INFTY, INFTY);
                } else {
                    findMove(board(), maxDepth(board()),
                            true, -1, -INFTY, INFTY);
                }
            }
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove,
                         int sense, int alpha, int beta) {
        if (board.winner() != null) {
            if (board.winner() == WHITE) {
                return WINNING_VALUE;
            } else {
                return -WINNING_VALUE;
            }
        }
        int savedval = staticScore(board);
        if (sense == 1) {
            if (saveMove && board.moveCount() > 0 && winWM != null) {
                _lastFoundMove = winWM;
                return WINNING_VALUE;
            }
            if (depth <= 0 || board.winner() != null) {
                return staticScore(board);
            }
            if (board.moveCount() <= CHECK1) {
                savedval = findMax(board, 1, alpha, beta, saveMove);
            } else if (board.moveCount() <= CHECK2) {
                savedval = findMax(board, 2, alpha, beta, saveMove);
            } else {
                savedval = findMax(board, depth, alpha, beta, saveMove);
            }
        } else if (sense == -1) {
            if (saveMove && board.moveCount() > 0 && winBW(absent) != null) {
                _lastFoundMove = winBW(absent);
                return -WINNING_VALUE;
            }
            if (depth <= 0 || board.winner() != null) {
                return staticScore(board);
            }
            savedval = findMin(board, depth, alpha, beta, saveMove);
        }
        return savedval;
    }

    /** Return a heuristically determined maximum search depth
     *  based on characteristics of BOARD.
     *  @return depth. */
    private static int maxDepth(Board board) {
        return 4;
    }

    /** Return a heuristic value for BOARD.
     * @return score. */
    private int staticScore(Board board) {
        Square findKing = board.kingPosition();
        int boardscore = 0;
        if (findKing == null) {
            return -WINNING_VALUE;
        }
        if (findKing.isEdge()) {
            return WINNING_VALUE;
        }
        if (board.moveCount() >= 5) {
            winWM = winWMF(board, findKing);
        }
        if (winWM != null) {
            return WILL_WIN_VALUE;
        }
        int sqNorth = 8 - findKing.row();
        int sqSouth = findKing.row();
        int sqEast = 8 - findKing.col();
        int sqWest = findKing.col();
        if (min(sqNorth, min(sqSouth, min(sqWest, sqEast))) <= 6) {
            boardscore += 10;
        }
        int numBpiece = board.pieceLocations(BLACK).size();
        int numWpiece = board.pieceLocations(WHITE).size();
        if (numWpiece >= 4) {
            for (int k = 4; k < numWpiece; k++) {
                boardscore += 4;
            }
        }
        if (numBpiece >= 10) {
            for (int k = 10; k < numBpiece; k++) {
                boardscore -= 2;
            }
        }
        Square adjN =
                findKing.rookMove(0, 1);
        Square adjS =
                findKing.rookMove(2, 1);
        Square adjE =
                findKing.rookMove(1, 1);
        Square adjW =
                findKing.rookMove(3, 1);
        absent.clear();
        absent.add(adjE);
        absent.add(adjN);
        absent.add(adjS);
        absent.add(adjW);
        if (board.moveCount() > 6 && helpBW(board, adjN, adjS,
                adjE, adjW, findKing) == 3) {
            return -WILL_WIN_VALUE;
        }
        if (helpBS(boardscore, board, adjN,
                adjS, adjE, adjW) == boardscore) {
            boardscore += 5;
        }
        return boardscore;
    }

    /** Checks if white BOARD has an obvious win, given KINGLOC for help.
     * @return the value denoting it.
     */
    Move winWMF(Board board, Square kingloc) {
        if (board.isLegal(kingloc, sq(kingloc.col(), 0))) {
            return Move.mv(kingloc, sq(kingloc.col(), 0));
        } else if (board.isLegal(kingloc, sq(kingloc.col(), 8))) {
            return Move.mv(kingloc, sq(kingloc.col(), 8));
        } else if (board.isLegal(kingloc, sq(0, kingloc.row()))) {
            return Move.mv(kingloc, sq(0, kingloc.row()));
        } else if (board.isLegal(kingloc, sq(8, kingloc.row()))) {
            return Move.mv(kingloc, sq(8, kingloc.row()));
        }
        return null;
    }

    /** Checks for king capture when king near throne with BOARD,
     * N, S, E, W, and FINDKING.
     * @return number of count. */
    int helpBW(Board board, Square N, Square S,
               Square E, Square W, Square findKing) {
        int count = 0;
        if (findKing == THRONE
                || findKing == NTHRONE
                || findKing == STHRONE
                || findKing == ETHRONE
                || findKing == WTHRONE) {
            if (E == THRONE
                    || board.get(E) == BLACK) {
                count += 1;
                absent.remove(E);
            }
            if (N == THRONE
                    || board.get(N) == BLACK) {
                count += 1;
                absent.remove(N);
            }
            if (S == THRONE
                    || board.get(S) == BLACK) {
                count += 1;
                absent.remove(S);
            }
            if (W == THRONE
                    || board.get(W) == BLACK) {
                count += 1;
                absent.remove(W);
            }
        }
        return count;
    }

    /** Helps determine BOARDSCORE with BOARD, N, S, E, W.
     * @return updated boardscore. */
    int helpBS(int boardScore, Board board, Square N, Square S,
               Square E, Square W) {
        if (N != null
                && board.get(N) == BLACK) {
            boardScore -= 7;
            absent.remove(N);
        }
        if (E != null
                && board.get(E) == BLACK) {
            boardScore -= 7;
            absent.remove(E);
        }
        if (S != null
                && board.get(S) == BLACK) {
            boardScore -= 7;
            absent.remove(S);
        }
        if (W != null
                && board.get(W) == BLACK) {
            boardScore -= 7;
            absent.remove(W);
        }
        return boardScore;
    }

    /** Finds the winning move for Black from a list of
     * NOTTHERE squares.
     * @return the winning move for Black. */
    Move winBW(ArrayList<Square> notthere) {
        if (notthere.size() == 1) {
            for (Move m : board().legalMoves(BLACK)) {
                if (m.to() == notthere.get(0)) {
                    return m;
                }
            }
        } else {
            if (notthere.contains(EMPTY)) {
                return null;
            } else {
                for (Square each : notthere) {
                    if (board().get(
                            each.rookMove(
                                    each.direction(
                                            board().kingPosition()), 2))
                            == BLACK) {
                        for (Move m : board().legalMoves(BLACK)) {
                            if (m.to() == each) {
                                return m;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    /** Prune for White pieces on POSN to DEPTH,
     * using ALPHA and BETA as guides, and SAVEMOVE if true.
     * @return value of move. */
    int findMax(Board posn, int depth,
                int alpha, int beta, boolean saveMove) {
        Board copy = new Board(posn);
        int savedval = -INFTY;
        for (Move M : posn.legalMoves(WHITE)) {
            copy.setTurn(WHITE);
            copy.makeMove(M);
            if (copy.winner() == null) {
                savedval = max(savedval,
                        findMove(copy, depth - 1,
                                false, -1, alpha, beta));
            } else {
                savedval = max(savedval, -WINNING_VALUE);
            }
            if (saveMove && _lastFoundMove == null) {
                _lastFoundMove = M;
            }
            if ((alpha < savedval) && saveMove) {
                _lastFoundMove = M;
            }
            alpha = max(alpha, savedval);
            if (alpha >= beta) {
                break;
            }
            copy.undo();
        }
        return savedval;
    }

    /** Prune for Black pieces on POSN to DEPTH,
     * using ALPHA and BETA as guides, and SAVEMOVE if true.
     * @return value of move. */
    int findMin(Board posn, int depth,
                int alpha, int beta, boolean saveMove) {
        Board copy = new Board(posn);
        int savedval = INFTY;
        for (Move M : posn.legalMoves(BLACK)) {
            copy.setTurn(BLACK);
            copy.makeMove(M);
            if (copy.winner() == null) {
                savedval = min(savedval,
                        findMove(copy, depth - 1,
                                false, 1, alpha, beta));
            } else {
                savedval = min(savedval, WINNING_VALUE);
            }
            if (saveMove && _lastFoundMove == null) {
                _lastFoundMove = M;
            }
            if ((beta < savedval) && saveMove) {
                _lastFoundMove = M;
            }
            beta = min(beta, savedval);
            if (alpha >= beta) {
                break;
            }
            copy.undo();
        }
        return savedval;
    }

    /** Stores the winning last move for white. */
    private Move winWM = null;
    /** Stores the adjacent unoccupied square. */
    private ArrayList<Square> absent = new ArrayList<Square>();
    /** Move limit to check to this depth. */
    private static final int CHECK1 = 5;
    /** Move limit to check to second depth. */
    private static final int CHECK2 = 34;

}
