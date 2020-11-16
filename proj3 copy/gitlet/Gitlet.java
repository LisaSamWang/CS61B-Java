package gitlet;

import java.io.File;
import java.io.ObjectInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.HashMap;

import static gitlet.Utils.*;

/** Equivalent to the repo of actual git.
 * @author Lisa Sam Wang. */
public class Gitlet implements Serializable {
    /** the head. */
    private String head = null;
    /** the branches. */
    private HashMap<String, String> _branches;
    /** the staging area. */
    private Stage _stagingArea;
    /** Marks unstaging/stages for removal. */
    private ArrayList<String> _untrackedFiles;
    /** helpers for remote. */
    private static String remotesdir = ".gitlet" + File.separator + "remotes";
    /** stores my remotes and their paths. */
    private HashMap<String, String> myremotes = new HashMap<String, String>();
    /** helps with remote. */
    private boolean helpremote = false;

    /** creates the gitlet. */
    public Gitlet() {
        init();
    }

    /** initializes the gitlet. */
    public void init() {
        File gitlet = new File(".gitlet");
        if (!gitlet.exists()) {
            Commit initcomm = new Commit("initial commit", null, null);
            gitlet.mkdir();
            File commits = new File(".gitlet/commits");
            commits.mkdir();
            File staging = new File(".gitlet/staging");
            staging.mkdir();
            File storage = new File(".gitlet/storage");
            storage.mkdir();
            File remotes = new File(".gitlet" + File.separator + "remotes");
            remotes.mkdir();

            String id = initcomm.getSha();
            File initialFile = new File(".gitlet/commits/" + id);
            writeObject(initialFile, initcomm);
            head = "master";
            _branches = new HashMap<String, String>();
            _branches.put("master", initcomm.getSha());

            _stagingArea = new Stage();
            _untrackedFiles = new ArrayList<String>();
        } else {
            System.out.println("A gitlet version control "
                    + "system already exists in the current directory");
            System.exit(0);
        }
    }

    /** does equivalent to git add the file TOADD. */
    @SuppressWarnings("unchecked")
    public void add(String toadd) {
        File f = new File(toadd);
        if (!f.exists()) {
            System.out.println("File does not exist.");
            System.exit(0);
        }
        Blob adding = new Blob(f);
        String fileHash = adding.content();
        Commit lastcomm = uidToCommit(_branches.get(head));
        HashMap<String, String> files = lastcomm.getfiles();
        File stagingBlob = new File(".gitlet/staging/" + fileHash);
        File storeBlob = new File(".gitlet/storage/" + fileHash);
        if (files == null || files.size() == 0
                || !files.containsKey(toadd)
                || !files.get(toadd).equals(fileHash)) {
            _stagingArea.put(toadd, fileHash);
            String contents = readContentsAsString(f);
            writeContents(stagingBlob, contents);
            writeContents(storeBlob, contents);
        } else if (files.containsKey(toadd)
                && fileHash.equals(files.get(toadd))) {
            stagingBlob.delete();
            _stagingArea.remove(toadd);
        } else {
            String prevhash =
                    _stagingArea.getfiles().get(toadd);
            File prevver = new File(".gitlet/staging/" + prevhash);
            prevver.delete();
            _stagingArea.put(toadd, fileHash);
            String contents = readContentsAsString(f);
            writeContents(storeBlob, contents);
            writeContents(stagingBlob, contents);
        }
        if (_untrackedFiles.contains(toadd)) {
            _untrackedFiles.remove(toadd);
        }
        save();
    }

    /** converts from the sha-1 code, or UID, to the commit.
     * @return Commit that matches the uid. */
    public Commit uidToCommit(String uid) {
        File f = new File(".gitlet/commits/" + uid);
        if (f.exists()) {
            return Utils.readObject(f, Commit.class);
        } else {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        return null;
    }

    /** does equivalent to git commit with the provided ARGS. */
    @SuppressWarnings("unchecked")
    public void commit(String args) {
        Commit mostRecent = uidToCommit(_branches.get(head));
        HashMap<String, String> trackedFiles = mostRecent.getfiles();
        HashMap<String, String> copytracked =
                (HashMap<String, String>) trackedFiles.clone();

        if (copytracked == null || copytracked.size() == 0) {
            trackedFiles = new HashMap<String, String>();
        }

        if (_stagingArea.getfiles().size() != 0
                || _untrackedFiles.size() != 0) {
            for (Object fileName : _stagingArea.getfiles().keySet()) {
                copytracked.put((String) fileName,
                        _stagingArea.getfiles().get(fileName));
            }
            for (String fileName : _untrackedFiles) {
                copytracked.remove(fileName);
            }
        } else {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit[] parent = new Commit[]{mostRecent};
        Commit newCommit = new Commit(args, copytracked, parent);
        String s = newCommit.getSha();
        File newCommFile = new File(".gitlet/commits/" + s);
        writeObject(newCommFile, newCommit);

        _stagingArea = new Stage();
        _untrackedFiles = new ArrayList<String>();
        _branches.put(head, newCommit.getSha());
        save();
    }

    /** commits for merges with ARG and MERGEDPARENT. */
    @SuppressWarnings("unchecked")
    public void commitmerge(String arg, Commit mergedparent) {
        Commit mostRecent = uidToCommit(_branches.get(head));
        HashMap<String, String> trackedFiles = mostRecent.getfiles();
        HashMap<String, String> copytracked =
                (HashMap<String, String>) trackedFiles.clone();

        if (copytracked == null || copytracked.size() == 0) {
            trackedFiles = new HashMap<String, String>();
        }

        if (_stagingArea.getfiles().size() != 0
                || _untrackedFiles.size() != 0) {
            for (Object fileName : _stagingArea.getfiles().keySet()) {
                copytracked.put((String) fileName,
                        _stagingArea.getfiles().get(fileName));
            }
            for (String fileName : _untrackedFiles) {
                copytracked.remove(fileName);
            }
        } else {
            System.out.println("No changes added to the commit.");
            System.exit(0);
        }
        Commit[] parent = new Commit[]{mostRecent, mergedparent};
        Commit newCommit = new Commit(arg, copytracked, parent);
        String s = newCommit.getSha();
        File newCommFile = new File(".gitlet/commits/" + s);
        writeObject(newCommFile, newCommit);

        _stagingArea = new Stage();
        _untrackedFiles = new ArrayList<String>();
        _branches.put(head, newCommit.getSha());
        save();
    }

    /** Takes in a String[] ARGS.
     */
    @SuppressWarnings("unchecked")
    public void checkout(String[] args) {
        String commID = null;
        String fileName = null;
        if (args.length == 2 && args[0].equals("--")) {
            fileName = args[1];
            commID = _branches.get(head);
        } else if (args.length == 3 && args[1].equals("--")) {
            commID = args[0];
            fileName = args[2];
        } else {
            System.out.println("Incorrect operands");
            System.exit(0);
        }
        commID = makelongID(commID);
        Commit comm = uidToCommit(commID);
        HashMap<String, String> trackedFiles = comm.getfiles();
        if (trackedFiles.containsKey(fileName)) {
            File f = new File(fileName);
            String p = ".gitlet/storage/";
            String blobFileName = p + trackedFiles.get(fileName);
            File g = new File(blobFileName);
            String contents = readContentsAsString(g);
            writeContents(f, contents);
        } else {
            System.out.println("File does not exist in that commit.");
            System.exit(0);
        }
    }

    /**
     * Takes in a shortened String ID and returns a String
     * of the full length ID.
     */
    private String makelongID(String id) {
        if (id.length() == UID_LENGTH) {
            return id;
        }
        File commitFolder = new File(".gitlet/commits");
        File[] commits = commitFolder.listFiles();
        for (File file : commits) {
            if (file.getName().contains(id)) {
                return file.getName();
            }
        }
        System.out.println("No commit with that id exists.");
        System.exit(0);
        return null;
    }

    /** This is the third use case for checkout.
     * It takes in a BRANCHNAME. */
    @SuppressWarnings("unchecked")
    public void checkout(String branchName) {
        if (!_branches.containsKey(branchName)) {
            System.out.println("No such branch exists.");
            System.exit(0);
        }
        if (head.equals(branchName)) {
            String s = "No need to checkout the current branch.";
            System.out.println(s);
            System.exit(0);
        }
        String curcommID = _branches.get(head);
        Commit curcomm = uidToCommit(curcommID);
        HashMap<String, String> curfiles = curcomm.getfiles();
        String commID = _branches.get(branchName);
        Commit comm = uidToCommit(commID);
        HashMap<String, String> files = comm.getfiles();
        String pwdString = System.getProperty("user.dir");
        File curdir = new File(pwdString);
        File[] curdirfiles = curdir.listFiles();
        for (File each : curdirfiles) {
            if (!curfiles.containsKey(each.getName())
                    && files.containsKey(each.getName())) {
                if (!helpremote) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it or add it first.");
                    System.exit(0);
                }
            }
        }
        helpremote = false;
        for (String each : files.keySet()) {
            String[] args = new String[]{commID, "--", each};
            checkout(args);
        }
        for (String each : curfiles.keySet()) {
            if (files.isEmpty()) {
                restrictedDelete(each);
                rm(each);
            } else {
                if (!files.containsKey(each)) {
                    restrictedDelete(each);
                    rm(each);
                }
            }
        }
        _stagingArea = new Stage();
        _untrackedFiles = new ArrayList<String>();
        head = branchName;

    }

    /**
     * Perform the command 'log'.
     */
    public void log() {
        String myhead = this._branches.get(head);
        while (myhead != null) {
            Commit first = uidToCommit(myhead);
            print(first);
            Commit[] parents = first.getMyparents();
            if (parents != null) {
                myhead = parents[0].getSha();
            } else {
                myhead = null;
            }
        }
    }

    /**
     * Print the commit TOPRINT.
     */
    public void print(Commit toprint) {
        if (toprint == null) {
            return;
        }
        System.out.println("===");
        System.out.format("commit %s\n", toprint.getSha());
        if (toprint.getMyparents() != null
                && toprint.getMyparents().length > 1) {
            System.out.format("Merge: %s %s\n",
                    toprint.getMyparents()[0].getSha().substring(0, 7),
                    toprint.getMyparents()[1].getSha().substring(0, 7));
        }
        SimpleDateFormat sdf =
                new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        String date = sdf.format(toprint.time());
        System.out.format("Date: %s\n", date);
        System.out.println(toprint.log());
        System.out.println();
    }

    /** prints out all logs. */
    public void globallog() {
        File commitFolder = new File(".gitlet/commits");
        File[] commits = commitFolder.listFiles();

        for (File file : commits) {
            print(uidToCommit(file.getName()));
        }
    }

    /** does equivalent to git rm filr TORMV. */
    @SuppressWarnings("unchecked")
    public void rm(String tormv) {
        File toremv = new File(tormv);
        Commit lastcomm = uidToCommit(_branches.get(head));
        HashMap<String, String> tracked = lastcomm.getfiles();
        int cond = 0;
        if (_stagingArea.getfiles().containsKey(tormv)) {
            cond++;
            String delhash = _stagingArea.getfiles().get(tormv);
            File unstage = new File(".gitlet/staging/" + delhash);
            unstage.delete();
            _stagingArea.remove(tormv);
        }
        if (!tracked.isEmpty() && tracked.containsKey(tormv)) {
            cond++;
            if (!_untrackedFiles.contains(tormv)) {
                _untrackedFiles.add(tormv);
            }
            restrictedDelete(toremv);
        }
        if (cond == 0) {
            System.out.println("No reason to remove the file.");
            System.exit(0);
        }
    }

    /** finds the commits with MESSAGE. */
    public void find(String message) {
        File commitFolder = new File(".gitlet/commits");
        File[] commits = commitFolder.listFiles();
        int count = 0;
        for (File each : commits) {
            if (uidToCommit(each.getName()).getMessage().equals(message)) {
                System.out.println(each.getName());
                count++;
            }
        }
        if (count == 0) {
            System.out.println("Found no commit with that message.");
            System.exit(0);
        }
    }

    /** does equivalent to git status. */
    public void status() {
        String[] sorted = _branches.keySet().toArray(new String[0]);
        Arrays.sort(sorted);
        System.out.println("=== Branches ===");
        for (String eachb : sorted) {
            if (eachb.equals(head)) {
                System.out.println("*" + eachb);
            } else {
                System.out.println(eachb);
            }
        }
        System.out.println();
        String[] sortedRmv =
                _stagingArea.getfiles().keySet().toArray(new String[0]);
        Arrays.sort(sortedRmv);
        System.out.println("=== Staged Files ===");
        for (String eachr : sortedRmv) {
            System.out.println(eachr);
        }
        System.out.println();
        String[] untrackedsorted =
                _untrackedFiles.toArray(new String[0]);
        Arrays.sort(untrackedsorted);
        System.out.println("=== Removed Files ===");
        for (String each : untrackedsorted) {
            System.out.println(each);
        }
        System.out.println();
        helpstatus();
    }

    /** helper for ec part of status. */
    @SuppressWarnings("unchecked")
    public void helpstatus() {
        System.out.println("=== Modifications Not Staged For Commit ===");
        ArrayList<String> toprint = new ArrayList<String>();
        Commit curcomm = uidToCommit(_branches.get(head));
        HashMap<String, String> tracked = curcomm.getfiles();
        String pwdString = System.getProperty("user.dir");
        File curdir = new File(pwdString);
        List<String> curdirsort = plainFilenamesIn(curdir);
        if (curdirsort != null) {
            for (String each : curdirsort) {
                File maketemp = new File(each);
                Blob temp = new Blob(maketemp);
                if (tracked.containsKey(temp.getname())
                        && !temp.content().equals(tracked.get(temp.getname()))
                        && !_stagingArea.getfiles().
                        containsValue(temp.content())) {
                    toprint.add(temp.getname() + " (modified)");
                } else if (_stagingArea.getfiles().containsKey(temp.getname())
                        && !_stagingArea.getfiles().get(
                                temp.getname()).equals(temp.content())) {
                    toprint.add(temp.getname() + " (modified)");
                }
            }
        }
        for (String name : _stagingArea.getfiles().keySet()) {
            File tempfile = new File(name);
            if (!tempfile.exists()) {
                toprint.add(name + " (deleted)");
            }
        }
        for (String eachkey : tracked.keySet()) {
            if (!_untrackedFiles.contains(eachkey)) {
                File check = new File(eachkey);
                if (!check.exists()) {
                    toprint.add(eachkey + " (deleted)");
                }
            }
        }
        if (toprint.size() != 0) {
            String[] toprintsorted = toprint.toArray(new String[0]);
            Arrays.sort(toprintsorted);
            for (String eachone : toprintsorted) {
                System.out.println(eachone);
            }
        }
        System.out.println();
        System.out.println("=== Untracked Files ===");
        if (curdirsort != null) {
            String[] arrcurdirsort = curdirsort.toArray(new String[0]);
            Arrays.sort(arrcurdirsort);
            for (String each : curdirsort) {
                if (!_stagingArea.getfiles().containsKey(each)
                        && !tracked.containsKey(each)) {
                    System.out.println(each);
                } else if (_untrackedFiles.contains(each)) {
                    System.out.println(each);
                }
            }
        }
    }

    /** creates the branch with NAME. */
    public void branch(String name) {
        if (_branches.containsKey(name)) {
            System.out.println("A branch with that name already exists.");
            System.exit(0);
        }
        _branches.put(name, _branches.get(head));
    }

    /** removes branch with NAME. */
    public void rmbranch(String name) {
        if (!_branches.containsKey(name)) {
            System.out.println("A branch with that name does not exist");
            System.exit(0);
        } else if (head.equals(name)) {
            System.out.println("Cannot remove the current branch.");
            System.exit(0);
        }
        _branches.remove(name);
    }

    /** resets the gitlet to head branch pointing to commit with ID. */
    @SuppressWarnings("unchecked")
    public void reset(String id) {
        String pwdString = System.getProperty("user.dir");
        File curdir = new File(pwdString);
        File[] curdirfiles = curdir.listFiles();
        String longid = makelongID(id);
        Commit wanted = uidToCommit(longid);
        HashMap<String, String> wantedtrack = wanted.getfiles();
        Commit curcomm = uidToCommit(_branches.get(head));
        HashMap<String, String> tracked = curcomm.getfiles();
        for (File each : curdirfiles) {
            if (!tracked.containsKey(each.getName())
                    && wantedtrack.containsKey(each.getName())) {
                if (!helpremote) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it or add it first.");
                    System.exit(0);
                }
            }
        }
        helpremote = false;
        for (String each : wantedtrack.keySet()) {
            String[] args = new String[] { longid, "--", each };
            checkout(args);
        }
        for (String eacht : tracked.keySet()) {
            if (!wantedtrack.containsKey(eacht)) {
                restrictedDelete(eacht);
            }
        }
        _stagingArea = new Stage();
        _branches.put(head, longid);
    }

    /** does equivalent to git merge with branch NAME. */
    @SuppressWarnings("unchecked")
    public void merge(String name) {
        String pwdString = System.getProperty("user.dir");
        File curdir = new File(pwdString);
        File[] curdirfiles = curdir.listFiles();
        Commit curr = uidToCommit(_branches.get(head));
        HashMap<String, String> curblobs = curr.getfiles();
        if (!_branches.containsKey(name)) {
            System.out.println("A branch with that name does not exist.");
            System.exit(0);
        }
        Commit given = uidToCommit(_branches.get(name));
        HashMap<String, String> givenblobs = given.getfiles();
        for (File each : curdirfiles) {
            if (!curblobs.containsKey(each.getName())
                    && givenblobs.containsKey(each.getName())) {
                if (!helpremote) {
                    System.out.println("There is an untracked file in the way;"
                            + " delete it or add it first.");
                    System.exit(0);
                }
            }
        }
        mergehelper(name);
        helpremote = false;
        Commit splitpt = splitpoint(name);
        if (splitpt.getSha().equals(_branches.get(name))) {
            System.out.println("Given branch is an "
                    + "ancestor of the current branch.");
            System.exit(0);
        } else if (splitpt.getSha().equals(_branches.get(head))) {
            String prevhead = head;
            checkout(name);
            head = prevhead;
            _branches.put(head, _branches.get(name));
            System.out.println("Current branch fast-forwarded.");
            System.exit(0);
        }
        HashMap<String, String> splitptfiles = splitpt.getfiles();
        helpsplitpt(splitptfiles, curblobs, givenblobs, given);
        for (String eachc : curblobs.keySet()) {
            if (!splitptfiles.containsKey(eachc)
                    && givenblobs.containsKey(eachc)
                    && !givenblobs.get(eachc).equals(curblobs.get(eachc))) {
                String p = ".gitlet/storage/";
                String cur = curblobs.get(eachc);
                String giv = givenblobs.get(eachc);
                mergeconflict(eachc, p, cur, giv);
            }
        }
        for (String eachg: givenblobs.keySet()) {
            if (!splitptfiles.containsKey(eachg)
                    && !curblobs.containsKey(eachg)) {
                String[] args = new String[]{given.getSha(), "--", eachg};
                checkout(args);
                add(eachg);
            }
        }
        commitmerge("Merged " + name + " into " + head + ".", given);
    }

    /** helps with merge errors with NAME. */
    public void mergehelper(String name) {
        if (!_stagingArea.getfiles().isEmpty()
                || _untrackedFiles.size() != 0) {
            if (!helpremote) {
                System.out.println("You have uncommited changes");
                System.exit(0);
            }
        } else if (name.equals(head)) {
            System.out.println("Cannot merge a branch with itself.");
            System.exit(0);
        }
    }
    /** handles merge conflicts with EACHC, P, CUR, GIV. */
    public void mergeconflict(String eachc, String p, String cur, String giv) {
        if (cur == null) {
            cur = "";
        }
        if (giv == null) {
            giv = "";
        }
        String contents = "<<<<<<< HEAD\n";
        if (!cur.isEmpty()) {
            File c = new File(p + cur);
            contents += Utils.readContentsAsString(c);
        } else {
            contents += cur;
        }
        contents += "=======\n";
        if (!giv.isEmpty()) {
            File g = new File(p + giv);
            contents += Utils.readContentsAsString(g);
        } else {
            contents += giv;
        }
        contents += ">>>>>>>\n";
        Utils.writeContents(new File(eachc), contents);
        add(eachc);
        Utils.message("Encountered a merge conflict.");
    }

    /** finds the split point with GIVEN branch.
     * @return Commit that is the split point. */
    public Commit splitpoint(String given) {
        Commit trackcurcom = uidToCommit(_branches.get(head));
        Commit trackgiven = uidToCommit(_branches.get(given));
        ArrayList<String> allgivencomparents = new ArrayList<String>();
        allgivencomparents.add(trackgiven.getSha());
        while (trackgiven.getMyparents() != null) {
            Commit[] parents = trackgiven.getMyparents();
            for (Commit eachpar : parents) {
                allgivencomparents.add(eachpar.getSha());
            }
            trackgiven = trackgiven.getMyparents()[0];
        }
        if (allgivencomparents.contains(trackcurcom.getSha())) {
            return trackcurcom;
        }
        while (trackcurcom.getMyparents() != null) {
            Commit[] curparents = trackcurcom.getMyparents();
            for (Commit each : curparents) {
                if (allgivencomparents.contains(
                        each.getSha())) {
                    return each;
                }
            }
            trackcurcom = trackcurcom.getMyparents()[0];
        }
        return null;
    }

    /** deals with the split point files in merge using SPLITPTFILES,
     * CURBLOBS, GIVENBLOBS, GIVEN. */
    public void helpsplitpt(HashMap<String, String> splitptfiles,
                            HashMap<String, String> curblobs,
                            HashMap<String, String> givenblobs, Commit given) {
        for (String blob : splitptfiles.keySet()) {
            if (curblobs.containsKey(blob)
                    && givenblobs.containsKey(blob)
                    && splitptfiles.get(blob).
                    equals(curblobs.get(blob))
                    && !splitptfiles.get(blob).
                    equals(givenblobs.get(blob))) {
                String[] args = new String[]{given.getSha(), "--", blob};
                checkout(args);
                add(blob);
            } else if (givenblobs.containsKey(blob)
                    && givenblobs.get(blob).equals(splitptfiles.get(blob))
                    && !curblobs.containsKey(blob)) {
                _stagingArea.remove(blob);
                restrictedDelete(blob);
                if (!_untrackedFiles.contains(blob)) {
                    _untrackedFiles.add(blob);
                }
            } else if (!givenblobs.containsKey(blob)
                    && splitptfiles.get(blob).equals(curblobs.get(blob))) {
                restrictedDelete(blob);
                rm(blob);
                _untrackedFiles.add(blob);
            } else if (!splitptfiles.get(blob).
                    equals(curblobs.get(blob))
                    && !splitptfiles.get(blob).
                    equals(givenblobs.get(blob))) {
                if ((curblobs.containsKey(blob)
                        && givenblobs.containsKey(blob)
                        && !curblobs.get(blob).equals(givenblobs.get(blob)))
                        || (curblobs.containsKey(blob)
                        && !givenblobs.containsKey(blob))
                        || (givenblobs.containsKey(blob)
                        && !curblobs.containsKey(blob))) {
                    String p = ".gitlet/storage/";
                    String cur = curblobs.get(blob);
                    String giv = givenblobs.get(blob);
                    mergeconflict(blob, p, cur, giv);
                }
            }
        }
    }

    /** adds the remote directory based on NAME and REMODIR. */
    public void addremote(String name, String remodir) {
        String remotename = name;
        String dir = remodir;
        myremotes.put(name, remodir);
        new File(remotesdir).mkdir();
        File myremotesdir = join(remotesdir, remotename);
        if (myremotesdir.exists()) {
            System.out.println("A remote with that name already exists.");
            System.exit(0);
        }
        writeObject(myremotesdir, dir);
    }

    /** does remove the remote NAME. */
    public void rmremote(String name) {
        String remotename = name;
        File myremotesdir = join(remotesdir, remotename);
        if (!myremotesdir.exists()) {
            System.out.println("A remote with that name does not exist.");
        } else {
            myremotesdir.delete();
        }
        myremotes.remove(name);
    }

    /** does push from remote NAME and REMOBRANCHNAME. */
    @SuppressWarnings("unchecked")
    public void push(String name, String remobranchname) {
        getremote(name);
        String remotedir = myremotes.get(name);
        File actualrmote = new File(remotedir);
        if (!actualrmote.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        Gitlet remotegitlet = getGitlet(remotedir);
        helppush(remotegitlet);
        if (!remotegitlet._branches.containsKey(remobranchname)) {
            remotegitlet.branch(remobranchname);
        }
        String remotelastsha = remotegitlet._branches.get(remobranchname);
        Commit trackcurcom = uidToCommit(_branches.get(head));
        ArrayList<Commit> currparents = new ArrayList<Commit>();
        if (!remotelastsha.equals(trackcurcom.getSha())) {
            currparents.add(trackcurcom);
        }
        Commit copytrackcurcom = trackcurcom;
        outer:
        while (copytrackcurcom.getMyparents() != null) {
            Commit[] curparents = copytrackcurcom.getMyparents();
            for (Commit each : curparents) {
                if (each.getSha().equals(remotelastsha)) {
                    break outer;
                }
                currparents.add(each);
            }
            copytrackcurcom = copytrackcurcom.getMyparents()[0];
        }
        for (Commit each : currparents) {
            String s = each.getSha();
            File newCommFile = new File(remotedir
                    + File.separator + "commits" + File.separator + s);
            writeObject(newCommFile, each);
            HashMap<String, String> trackedFiles = each.getfiles();
            for (String eachf : trackedFiles.keySet()) {
                File file = new File(eachf);
                String contents = readContentsAsString(file);
                String p = remotedir + File.separator
                        + "storage" + File.separator;
                String blobFileName = p + trackedFiles.get(eachf);
                File g = new File(blobFileName);
                writeContents(g, contents);
            }
        }
        remotegitlet._branches.put(remobranchname, trackcurcom.getSha());
        remotegitlet.saveremote(remotedir);
    }

    /** helps check error for push using REMOTEGITLET. */
    public void helppush(Gitlet remotegitlet) {
        Commit trackcurcom = uidToCommit(_branches.get(head));
        ArrayList<String> allgivencurparents = new ArrayList<String>();
        allgivencurparents.add(trackcurcom.getSha());
        while (trackcurcom.getMyparents() != null) {
            Commit[] curparents = trackcurcom.getMyparents();
            for (Commit each : curparents) {
                allgivencurparents.add(each.getSha());
            }
            trackcurcom = trackcurcom.getMyparents()[0];
        }
        if (!allgivencurparents.contains(remotegitlet._branches.get(head))) {
            System.out.println("Please pull down remote "
                    + "changes before pushing.");
            System.exit(0);
        }
    }

    /** does fetch from NAME and REMOBRANCHNAME. */
    @SuppressWarnings("unchecked")
    public void fetch(String name, String remobranchname) {
        helpremote = true;
        getremote(name);
        String path = myremotes.get(name);
        File remotedir = new File(path);
        if (!remotedir.exists()) {
            System.out.println("Remote directory not found.");
            System.exit(0);
        }
        Gitlet remotegitlet = getGitlet(path);
        if (!remotegitlet._branches.containsKey(remobranchname)) {
            System.out.println("That remote does not have that branch.");
            System.exit(0);
        }
        String remotelastsha = remotegitlet._branches.get(remobranchname);
        Commit remotecomm = null;
        File f = new File(path + File.separator
                + "commits" + File.separator + remotelastsha);
        if (f.exists()) {
            remotecomm = readObject(f, Commit.class);
        } else {
            System.out.println("No commit with that id exists.");
            System.exit(0);
        }
        List<Commit> remoteparents = new ArrayList<Commit>();
        remoteparents.add(remotecomm);
        File commitFolder = new File(".gitlet/commits");
        ArrayList<String> commshas = new ArrayList<String>();
        File[] commits = commitFolder.listFiles();
        for (File each : commits) {
            commshas.add(each.getName());
        }
        if (!commshas.contains(remotecomm.getSha())) {
            File newCommFile = new File(".gitlet/commits/"
                    + remotecomm.getSha());
            writeObject(newCommFile, remotecomm);
            helperremotes(path, remotecomm);
        }
        Commit copyremotecomm = remotecomm;
        while (copyremotecomm.getMyparents() != null) {
            Commit[] remparents = copyremotecomm.getMyparents();
            for (Commit each : remparents) {
                if (!commshas.contains(each.getSha())) {
                    String s = each.getSha();
                    File newCommFile = new File(".gitlet/commits/" + s);
                    writeObject(newCommFile, each);
                    helperremotes(path, remotecomm);
                }
            }
            copyremotecomm = copyremotecomm.getMyparents()[0];
        }
        if (_branches.containsKey(name + "/" + remobranchname)) {
            _branches.put(name + "/" + remobranchname, remotecomm.getSha());
        } else {
            branch(name + "/" + remobranchname);
            _branches.put(name + "/" + remobranchname, remotecomm.getSha());
        }
    }

    /** helper for remote with PATH, REMOTECOMM. */
    @SuppressWarnings("unchecked")
    public void helperremotes(String path, Commit remotecomm) {
        HashMap<String, String> trackedFiles
                = remotecomm.getfiles();
        for (String eachf : trackedFiles.keySet()) {
            File file = new File(eachf);
            String p = path + File.separator
                    + "storage" + File.separator;
            String blobFileName = p + trackedFiles.get(eachf);
            File g = new File(blobFileName);
            String contents = readContentsAsString(g);
            writeContents(file, contents);
            add(eachf);
            helpremote = true;
        }
    }

    /** does pull from remote NAME and REMOBRANCHNAME. */
    public void pull(String name, String remobranchname) {
        fetch(name, remobranchname);
        merge(name + "/" + remobranchname);
    }

    /** gets the existing remote with NAME.
     * @return the string path for remote. */
    public static String getremote(String name) {
        File remote = join(remotesdir, name);
        if (!remote.exists()) {
            System.out.println("A remote with that name does not exist.");
            System.exit(0);
        }
        String remotedirpath = readContentsAsString(remote);
        return remotedirpath;
    }

    /** getter method for staging area.
     * @return the staging area. */
    public Stage getstagingArea() {
        return _stagingArea;
    }

    /**
     * SAVE.
     */
    public void save() {
        File outFile = new File(".gitlet"
                + File.separator + "repoSerialized");
        try {
            ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(this);
            out.close();
        } catch (IOException excp) {
            System.out.println("IO EXCEPTION IN GITLET.JAVA INIT"
                    + excp.getMessage());
            excp.printStackTrace();
        }
    }

    /**
     * SAVE remote dir reachable through PATH.
     */
    public void saveremote(String path) {
        File outFile = new File(path
                + File.separator + "repoSerialized");
        try {
            ObjectOutputStream out =
                    new ObjectOutputStream(new FileOutputStream(outFile));
            out.writeObject(this);
            out.close();
        } catch (IOException excp) {
            System.out.println("IO EXCEPTION IN GITLET.JAVA INIT"
                    + excp.getMessage());
            excp.printStackTrace();
        }
    }

    /**
     * RETURN myGitlet given by PATH.
     */
    public static Gitlet getGitlet(String path) {
        Gitlet myGitlet;
        File inFile = new File(path + File.separator + "repoSerialized");
        try {
            ObjectInputStream inp =
                    new ObjectInputStream(new FileInputStream(inFile));
            myGitlet = (Gitlet) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            System.out.println("IO EXCEPTION IN GITLET.JAVA GETGITLET"
                    + excp.getMessage());
            excp.printStackTrace();
            myGitlet = null;
        }
        return myGitlet;
    }

}
