package gitlet;

import java.io.IOException;

/** Driver class for Gitlet, the tiny stupid version-control system.
 *  @author Zachary Zhang
 */
public class Main {

    /** Usage: java gitlet.Main ARGS, where ARGS contains
     *  <COMMAND> <OPERAND> .... */
    public static void main(String... args) throws IOException {
        Git tracker = new Git();
        if (args.length == 0) {
            System.out.println("Please enter a command.");
        } else if (args[0].equals("init")) {
            tracker.init();
        } else if (args[0].equals("add")) {
            tracker.add(args[1]);
        } else if (args[0].equals("commit")) {
            if (args.length == 1) {
                tracker.commits("", null);
            } else {
                tracker.commits(args[1], null);
            }
        } else if (args[0].equals("log")) {
            tracker.log();
        } else if (args[0].equals("checkout")) {
            if (args.length == 2) {
                tracker.checkoutBranch(args[1]);
            } else if (args.length == 3) {
                tracker.checkoutChangeVersion(args[2]);
            } else if (args.length == 4) {
                if (!args[2].equals("--")) {
                    System.out.println("Incorrect operands.");
                } else {
                    tracker.checkoutOverwrite(args[1], args[3]);
                }
            }
        } else if (args[0].equals("rm")) {
            tracker.rm(args[1]);
        } else if (args[0].equals("global-log")) {
            tracker.global();
        } else if (args[0].equals("find")) {
            tracker.find(args[1]);
        } else if (args[0].equals("status")) {
            tracker.status();
        } else if (args[0].equals("branch")) {
            tracker.branch(args[1]);
        } else if (args[0].equals("rm-branch")) {
            tracker.rmbranch(args[1]);
        } else if (args[0].equals("reset")) {
            tracker.reset(args[1]);
        } else if (args[0].equals("merge")) {
            tracker.mergeFailure(args[1]);
        } else if (args[0].equals("add-remote")) {
            tracker.addRemote(args[1], args[2]);
        } else if (args[0].equals("rm-remote")) {
            tracker.rmRemote(args[1]);
        } else if (args[0].equals("push")) {
            tracker.push(args[1], args[2]);
        } else if (args[0].equals("fetch")) {
            tracker.fetch(args[1], args[2]);
        } else if (args[0].equals("pull")) {
            tracker.pull(args[1], args[2]);
        } else {
            System.out.println("No command with that name exists.");
        }
        System.exit(0);
    }

}
