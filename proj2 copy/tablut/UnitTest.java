package tablut;

import org.junit.Test;
import static org.junit.Assert.*;
import ucb.junit.textui;

/** The suite of all JUnit tests for the enigma package.
 *  @author Lisa Sam Wang
 */
public class UnitTest {

    /** Run the JUnit tests in this package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    @Test
    public void testBoard() {
        Board myboard = new Board();
        System.out.println(myboard);
    }

    @Test
    public void testCopy() {
        Board modelBoard = new Board();
        modelBoard.makeMove(Square.sq(1, 6), Square.sq(1, 7));
        Board mycopy = new Board(modelBoard);
        assertEquals(modelBoard.encodedBoard(), mycopy.encodedBoard());
    }

    @Test
    public void testUndo() {
        Board myboard = new Board();
        myboard.makeMove(Square.sq("a", "6"), Square.sq("a", "8"));
        myboard.undo();
        Board newBoard = new Board();
        assertEquals(myboard.encodedBoard(), newBoard.encodedBoard());
    }

    @Test
    public void testCapture() {

    }




}


