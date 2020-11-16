package gitlet;

import ucb.junit.textui;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.*;

/** The suite of all JUnit tests for the gitlet package.
 *  @author Lisa Sam Wang
 */
public class UnitTest {

    /** Run the JUnit tests in the loa package. Add xxxTest.class entries to
     *  the arguments of runClasses to run other JUnit tests. */
    public static void main(String[] ignored) {
        textui.runClasses(UnitTest.class);
    }

    /** A dummy test to avoid complaint. */
    @Test
    public void placeholderTest() {
    }

    /** tests that nothing works without init. */
    @Test
    public void testnoinit() {
        File initial = new File(".gitlet");
        assertFalse(initial.exists());
    }

    /** tests that init works. */
    @Test
    public void testinit() {
        String[] mycommand = new String[] { "init" };
        Main.applycomm(mycommand);
        File initial = new File(".gitlet");
        assertTrue(initial.exists());
    }

    /** tests that commits subdir exists. */
    @Test
    public void testcommits() {
        File commits = new File(".gitlet/commits");
        assertTrue(commits.exists());
    }

    /** test that initial commit works. */
    @Test
    public void testinitcomm() {
        File initcomm = new File(".gitlet/commits");
        assertTrue(initcomm.listFiles() != null);
    }

    /** tests that subdir staging exists. */
    @Test
    public void teststaging() {
        File stagingarea = new File(".gitlet/staging");
        assertTrue(stagingarea.exists());
    }

    /** tests that storage exists. */
    @Test
    public void teststorage() {
        File storage = new File(".gitlet/storage");
        assertTrue(storage.exists());
    }

    /** check that remotes is a directory. */
    @Test
    public void testremoteisdir() {
        File remotedir = new File(".gitlet/remotes");
        assertTrue(remotedir.isDirectory());
    }

    /** check that commits is a directory. */
    @Test
    public void testcommdir() {
        File commdir = new File(".gitlet/commits");
        assertTrue(commdir.isDirectory());
    }

}


