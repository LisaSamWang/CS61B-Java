package gitlet;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

import static gitlet.Utils.join;

/**
 * Driver class for Gitlet, the tiny stupid version-control system.
 *
 * @author Lisa Sam Wang
 */
public class Main {

    /**
     * Usage: java gitlet.Main ARGS, where ARGS contains
     * <COMMAND> <OPERAND> ....
     */
    public static void main(String... args) {
        if (args.length == 0) {
            System.out.println("Please enter a command.");
            System.exit(0);
        }
        String firstParam = args[0];
        if (!join(System.getProperty("user.dir"),
                ".gitlet").exists() && !firstParam.equals("init")) {
            System.out.println("Not in an initialized Gitlet directory.");
            System.exit(0);
        }
        File gitlet = new File(".gitlet");
        if (gitlet.exists()) {
            myGitlet = getGitlet();
            applycomm(args);
        } else {
            applycomm(args);
        }
    }

    /**
     * Applies commands from ARGS.
     */
    public static void applycomm(String[] args) {
        String firstParam = args[0];
        String secondParam = null;
        String thirdParam = null;
        String fourthParam = null;
        if (args.length > 1) {
            secondParam = args[1];
            if (args.length > 2) {
                thirdParam = args[2];
                if (args.length > 3) {
                    fourthParam = args[3];
                }
            }
        }
        helper(firstParam, secondParam, thirdParam, fourthParam);
        myGitlet.save();
    }

    /** helps with FIRSTPARAM, SECONDPARAM, THIRDPARAM, FOURTHPARAM. */
    public static void helper(String firstParam, String secondParam,
                              String thirdParam, String fourthParam) {
        if (firstParam.equals("init")) {
            myGitlet = new Gitlet();
        } else if (firstParam.equals("status")) {
            myGitlet.status();
        } else if (firstParam.equals("log")) {
            myGitlet.log();
        } else if (firstParam.equals("global-log")) {
            myGitlet.globallog();
        } else if (secondParam == null) {
            System.out.println("Incorrect operands.");
        } else if (firstParam.equals("add")) {
            myGitlet.add(secondParam);
        } else if (firstParam.equals("commit")) {
            if (secondParam.equals("") || secondParam.isEmpty()) {
                System.out.println("Please enter a commit message.");
            } else {
                myGitlet.commit(secondParam);
            }
        } else if (firstParam.equals("merge")) {
            if (secondParam.equals("") || secondParam.isEmpty()) {
                System.out.println("Please enter a branch.");
            } else if (myGitlet.getstagingArea() != null
                    && !myGitlet.getstagingArea().getfiles().isEmpty()) {
                System.out.println("You have uncommitted changes.");
            } else {
                myGitlet.merge(secondParam);
            }
        } else if (firstParam.equals("rm")) {
            myGitlet.rm(secondParam);
        } else if (firstParam.equals("find")) {
            myGitlet.find(secondParam);
        } else if (firstParam.equals("branch")) {
            myGitlet.branch(secondParam);
        } else if (firstParam.equals("rm-branch")) {
            myGitlet.rmbranch(secondParam);
        } else if (firstParam.equals("reset")) {
            myGitlet.reset(secondParam);
        } else if (firstParam.equals("add-remote")) {
            myGitlet.addremote(secondParam, thirdParam);
        } else if (firstParam.equals("rm-remote")) {
            myGitlet.rmremote(secondParam);
        } else if (firstParam.equals("push")) {
            myGitlet.push(secondParam, thirdParam);
        } else if (firstParam.equals("fetch")) {
            myGitlet.fetch(secondParam, thirdParam);
        } else if (firstParam.equals("pull")) {
            myGitlet.pull(secondParam, thirdParam);
        } else if (firstParam.equals("checkout")) {
            helpcheckout(secondParam, thirdParam, fourthParam);
        } else {
            System.out.println("No command with that name exists.");
            System.exit(0);
        }
    }

    /** helps apply checkout with SECONDPARAM,
     * THIRDPARAM and FOURTHPARAM. */
    public static void helpcheckout(String secondParam,
                                    String thirdParam, String fourthParam) {
        if (secondParam.equals("--")) {
            if (thirdParam == null) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            String[] params = new String[]{secondParam, thirdParam};
            myGitlet.checkout(params);
        } else if (!(thirdParam == null)) {
            if (fourthParam == null || !thirdParam.equals("--")) {
                System.out.println("Incorrect operands.");
                System.exit(0);
            }
            String[] params =
                    new String[]{secondParam, thirdParam, fourthParam};
            myGitlet.checkout(params);
        } else {
            myGitlet.checkout(secondParam);
        }
    }

    /**
     * RETURN myGitlet.
     */
    public static Gitlet getGitlet() {
        File inFile = new File(".gitlet" + File.separator + "repoSerialized");
        try {
            ObjectInputStream inp =
                    new ObjectInputStream(new FileInputStream(inFile));
            myGitlet = (Gitlet) inp.readObject();
            inp.close();
        } catch (IOException | ClassNotFoundException excp) {
            System.out.println("IO EXCEPTION IN MAIN.JAVA GETGITLET"
                    + excp.getMessage());
            excp.printStackTrace();
            myGitlet = null;
        }
        return myGitlet;
    }

    /** the Gitlet. */
    private static Gitlet myGitlet;

}
