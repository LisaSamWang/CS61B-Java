package gitlet;

import java.io.Serializable;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;

import static gitlet.Utils.sha1;

/** makes commits.
 * @author Lisa Sam Wang.
 */
public class Commit implements Serializable {
    /** the message. */
    private String message;
    /** array of parents. */
    private Commit[] myparents;
    /** Hashmap where first string is file name like hi.txt,
     * and string two is the sha1 code. */
    private HashMap<String, String> myblobs;
    /** the timestamp. */
    private Date mytime;
    /** the sha1 code. */
    private String sha = null;

    /** constructs a commit with M, FILES, and PARENTS. */
    public Commit(String m, HashMap<String, String> files, Commit[] parents) {
        message = m;
        myparents = parents;
        mytime = new Date(Instant.now().getEpochSecond());
        if (files != null) {
            myblobs = files;
        } else {
            myblobs = new HashMap<String, String>();
        }
        if (getSha() == null) {
            makeSha();
        }
    }

    /** getter method for parents.
     * @return commit array.
     */
    public Commit[] getMyparents() {
        return myparents;
    }

    /** creates the sha1 code. */
    public void makeSha() {
        if (myparents == null) {
            sha = sha1(mytime.toString(), message,
                    myblobs.toString());
            return;
        }
        String parent1 = myparents[0].getSha();
        if (myparents.length > 1) {
            String parent2 = myparents[1].getSha();
            sha = sha1(mytime.toString(), message, parent1, parent2,
                    myblobs.toString());
        } else {
            sha = sha1(mytime.toString(), message, parent1,
                    myblobs.toString());
        }
    }

    /** getter method for the sha1 code.
     * @return the string of sha1.
     */
    public String getSha() {
        return sha;
    }

    /** getter method for the message.
     * @return string message.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return Date.
     */
    public Date time() {
        return mytime;
    }

    /**
     * @return Log.
     */
    public String log() {
        return message;
    }

    /**
     * @return the hashmap.
     */
    public HashMap getfiles() {
        return myblobs;
    }

}
