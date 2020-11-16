package gitlet;

import java.io.Serializable;
import java.util.HashMap;

/** creates the staging area.
 * @author Lisa Sam Wang.
 */
public class Stage implements Serializable {
    /**
     * Creates the snapshot before committing.
     */
    Stage() {
        snapshot = new HashMap<String, String>();
    }
    /**
     * @param  name is the file name.
     * @param  sha1 is the file sha1.
     */
    void put(String name, String sha1) {
        snapshot.put(name, sha1);
    }

    /**
     * @param  name is the file name.
     */
    void remove(String name) {
        snapshot.remove(name);
    }

    /**
     * @return hashmap.
     */
    HashMap<String, String> getfiles() {
        return snapshot;
    }

    /**stores a list of blobs.*/
    private HashMap<String, String> snapshot;

}
