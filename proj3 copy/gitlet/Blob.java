package gitlet;

import java.io.File;
import java.io.Serializable;

import static gitlet.Utils.*;

/** blobs storing contents of files.
 * @author Lisa Sam Wang.
 */
public class Blob implements Serializable {
    /** creates a blob from FILE. */
    Blob(File file) {
        _blob = sha1(readContentsAsString(file));
        _name = file.getName();
    }

    /**
     * @return file name
     * get file name.*/
    String getname() {
        return _name;
    }

    /**
     *@return file content in bytes
     *get file content./*/
    String content() {
        return _blob;
    }

    /**
     *file content.*/
    private String _blob;

    /**file name.*/
    private String _name;

}
