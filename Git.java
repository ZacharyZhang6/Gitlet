package gitlet;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.HashSet;

/** Git method to execute all the commands.
 *  @author Zachary Zhang
 */
public class Git implements Serializable {

    /** Init the gitlet folder with all the folders in it. */
    public void init() throws IOException {
        File gitlet = new File(".gitlet");
        if (gitlet.exists()) {
            System.out.println("A Gitlet version-control "
                    + "system already exists in the current directory.");
        } else {
            File staging = new File(".gitlet/staging");
            File stagingAdd = new File(".gitlet/"
                    + "staging/add");
            File stagingRemove = new File(".gitlet/staging/remove");
            File commit = new File(".gitlet/commit");
            File head = new File(".gitlet/head");
            File branch = new File(".gitlet/branch");
            File remote = new File(".gitlet/remote");
            remote.mkdir();
            gitlet.mkdir();
            staging.mkdir();
            stagingAdd.mkdir();
            stagingRemove.mkdir();
            commit.mkdir();
            head.mkdir();
            branch.mkdir();
            HashMap<String, String> store = new HashMap<>();
            Commit init = new Commit("initial commit", null, null, store);
            String id = init.toString();
            File initial = Utils.join(commit, id);
            File join = Utils.join(head, "head");
            File masters = Utils.join(branch, "*master");
            Utils.writeObject(join, init);
            Utils.writeObject(masters, init);
            Utils.writeObject(initial, init);
        }
    }

    /** Add file to the staging area.
     * @param fileName name of the file passed in
     * */
    public void add(String fileName) {
        File stagingAdd = new File(".gitlet/staging/add");
        File stagingRemove = new File(".gitlet/staging/remove");
        File head = new File(".gitlet/head/head");
        File added = new File(".", fileName);
        if (!added.exists()) {
            System.out.println("File does not exist.");
        } else {
            if (stagingRemove.length() != 0) {
                for (File i: stagingRemove.listFiles()) {
                    if (i.getName().equals(fileName)) {
                        i.delete();
                        return;
                    }
                }
            }
            HashMap<String, String> store = new HashMap<>();
            Commit read = Utils.readObject(head, Commit.class);
            store = read.getBlob();
            String content = Utils.readContentsAsString(added);
            if (store.containsKey(fileName)
                    && store.get(fileName).equals(content)) {
                File i = Utils.join(stagingAdd, fileName);
                i.delete();
                return;
            }
            File newAdd = Utils.join(stagingAdd, fileName);
            Utils.writeContents(newAdd, content);
        }
    }

    /** Commit the commit to commit file.
     * @param message log message of the commit
     * @param given second parent of the commit
     * */
    public void commits(String message, Commit given) {
        if (message.equals("")) {
            System.out.println("Please enter a commit message.");
            return;
        }
        File stagingAdd = new File(".gitlet/staging/add");
        File branch = new File(".gitlet/branch");
        File stagingRemove = new File(".gitlet/staging/remove");
        File commit = new File(".gitlet/commit");
        File head = new File(".gitlet/head/head");
        if (stagingAdd.listFiles().length == 0
                && stagingRemove.listFiles().length == 0) {
            System.out.println("No changes added to the commit.");
            return;
        } else {
            HashMap<String, String> store = new HashMap<>();
            Commit read = Utils.readObject(head, Commit.class);
            store = read.getBlob();
            HashMap<String, String> temp = new HashMap<>();
            for (String i: store.keySet()) {
                temp.put(i, store.get(i));
            }
            for (File i: stagingAdd.listFiles()) {
                String s = Utils.readContentsAsString(i);
                temp.put(i.getName(), s);
                i.delete();
            }
            for (File i: stagingRemove.listFiles()) {
                temp.remove(i.getName());
                i.delete();
            }
            Commit newCommit = new Commit(message, read, given, temp);
            File record = Utils.join(commit, newCommit.toString());
            Utils.writeObject(record, newCommit);
            Utils.writeObject(head, newCommit);
            for (File i: branch.listFiles()) {
                if (i.getName().startsWith("*")) {
                    Utils.writeObject(i, newCommit);
                    Commit check = Utils.readObject(i, Commit.class);
                    break;
                }
            }
        }

    }

    /** Remove the file.
     * @param fileName name of the file to be removed.
     * */
    public void rm(String fileName) {
        HashMap<String, String> store = new HashMap<>();
        File head = new File(".gitlet/head/head");
        Commit read = Utils.readObject(head, Commit.class);
        store = read.getBlob();
        File stagingAdd = new File(".gitlet/staging/add");
        File stagingRemove = new File(".gitlet/staging/remove");
        File remove = Utils.join(stagingRemove, fileName);
        File current = Utils.join(".", fileName);
        boolean check = false;
        for (File i: stagingAdd.listFiles()) {
            if (i.getName().equals(fileName)) {
                i.delete();
                check = true;
                return;
            }
        }
        boolean stage = false;
        for (String i: store.keySet()) {
            if (i.equals(fileName)) {
                String s = store.get(i);
                Utils.writeContents(remove, s);
                current.delete();
                stage = true;
                return;
            }
        }
        if (!check && !stage) {
            System.out.println("No reason to remove the file.");
        }
    }
    /** Print out the commits starting from head to ancestors.*/
    public void log() {
        File head = new File(".gitlet/head/head");
        Commit track = Utils.readObject(head, Commit.class);
        while (track != null) {
            System.out.println(print(track));
            track = track.getParent();
        }

    }

    /** Print out all the commits. */
    public void global() {
        File commit = new File(".gitlet/commit");
        for (File i: commit.listFiles()) {
            Commit j = Utils.readObject(i, Commit.class);
            System.out.println(print(j));
        }
    }

    /** Find commit with the given message.
     * @param message the message of the commit.
     * */
    public void find(String message) {
        File commit = new File(".gitlet/commit");
        boolean find = false;
        for (File i: commit.listFiles()) {
            Commit j = Utils.readObject(i, Commit.class);
            if (j.getMessage().equals(message)) {
                System.out.println(j.toString());
                find = true;
            }
        }
        if (!find) {
            System.out.println("Found no commit with that message.");
        }

    }

    /** Helper function for log and global log to print out commits.
     * @param commit commit to print
     * @return String to print.
     * */
    public String print(Commit commit) {
        String headLine = "===\n";
        String commitLine = "commit " + commit.toString() + "\n";
        String merge = "";
        if (commit.getSecondParent() != null) {
            merge = "Merge: " + commit.getParent().
                    toString().substring(0, 7) + " "
                    + commit.getSecondParent().
                    toString().substring(0, 7) + "\n";
        }
        String date = "Date: " + commit.getDate() + "\n";
        String commitMessage = commit.getMessage() + "\n";
        return headLine + commitLine + merge + date + commitMessage;
    }

    /** Print the status of all the files in each section. */
    public void status() {
        File gitlet = new File("./.gitlet");
        if (!gitlet.exists()) {
            System.out.println("Not in an "
                    + "initialized Gitlet directory.");
            return;
        }
        File working = new File(".");
        File stagingAdd = new File(".gitlet"
                + "/staging/add");
        File stagingRemove = new File(".gitlet"
                + "/staging/remove");
        File commit = new File(".gitlet/commit");
        File head = new File(".gitlet/head/head");
        Commit current = Utils.readObject(head, Commit.class);
        HashMap<String, String> content = current.getBlob();
        File branch = new File(".gitlet/branch");
        System.out.println("=== Branches ===");
        ArrayList<String> sort = new ArrayList<>();
        String original = "";
        for (File i: branch.listFiles()) {
            if (i.getName().startsWith("*")) {
                original = i.getName().substring(1);
                sort.add(original);
            } else {
                sort.add(i.getName());
            }
        }
        Collections.sort(sort);
        for (String i: sort) {
            if (i.equals(original)) {
                System.out.println("*" + i);
            } else {
                System.out.println(i);
            }
        }
        System.out.println("");
        System.out.println("=== Staged Files ===");
        sortFile(stagingAdd);
        System.out.println("");
        System.out.println("=== Removed Files ===");
        sortFile(stagingRemove);
        System.out.println("");
        System.out.println("=== Modifications Not Staged For Commit ===");
        sortPrint(modification());
        System.out.println("");
        System.out.println("=== Untracked Files ===");
        List<String> answer = new ArrayList<>();
        List<String> add = Utils.plainFilenamesIn(stagingAdd);
        for (String i: Utils.plainFilenamesIn(working)) {
            if (!add.contains(i) && !content.containsKey(i)) {
                answer.add(i);
            }
        }
        sortPrint(answer);
    }

    /** Helper function for status to print modified files.
     * @return the list of string to print in status method.
     * */
    public List<String> modification() {
        File working = new File(".");
        File stagingAdd = new File(".gitlet"
                + "/staging/add");
        File stagingRemove = new File(".gitlet"
                + "/staging/remove");
        File commit = new File(".gitlet/commit");
        File head = new File(".gitlet/head/head");
        Commit current = Utils.readObject(head, Commit.class);
        HashMap<String, String> content = current.getBlob();
        List<String> result = new ArrayList<>();
        List<String> dir = Utils.plainFilenamesIn(working);
        List<String> remove = Utils.plainFilenamesIn(stagingRemove);
        List<String> add = Utils.plainFilenamesIn(stagingAdd);
        for (String i: Utils.plainFilenamesIn(working)) {
            File temp = Utils.join(working, i);
            String work = Utils.readContentsAsString(temp);
            if (content.containsKey(i)
                    && !content.get(i).equals(work) && !add.contains(i)) {
                result.add(i + " (modified)");
            }
            for (File j: stagingAdd.listFiles()) {
                String check = Utils.readContentsAsString(j);
                if (i.equals(j.getName()) && !check.equals(work)) {
                    result.add(j + " (modified)");
                }
            }
        }
        for (String j: Utils.plainFilenamesIn(stagingAdd)) {
            if (!dir.contains(j)) {
                result.add(j + " (deleted)");
            }
        }
        for (String k: content.keySet()) {
            if (!remove.contains(k) && !dir.contains(k)) {
                result.add(k + " (deleted)");
            }
        }
        return result;
    }

    /** Helper function to print the file in the arraylist.
     * @param sort List with string ready to print.
     * */
    public void sortPrint(List<String> sort) {
        Collections.sort(sort);
        for (String i: sort) {
            System.out.println(i);
        }
    }

    /** Helper function to print the file in the sorted list.
     * @param name file name to print.
     * */
    public void sortFile(File name) {
        List<String> sort = Utils.plainFilenamesIn(name);
        for (String i: sort) {
            System.out.println(i);
        }
    }

    /** Checkout a file with that filename.
     * @param fileName the file to checkout version.
     * */
    public void checkoutChangeVersion(String fileName) {
        File change = new File(".", fileName);
        File head = new File(".gitlet/head/head");
        HashMap<String, String> store = new HashMap<>();
        Commit read = Utils.readObject(head, Commit.class);
        store = read.getBlob();
        boolean check = false;
        for (String i: store.keySet()) {
            if (i.equals(fileName)) {
                String s = store.get(i);
                Utils.writeContents(change, s);
                check = true;
            }
        }
        if (!check) {
            System.out.println("File does not exist in that commit.");
        }
    }

    /** Checkout a file with that filename and commit id.
     * @param fileName the filename to checkout version.
     * @param id the commit id to overwrite with.
     * */
    public void checkoutOverwrite(String id, String fileName) {
        File change = new File(".", fileName);
        File commit = new File(".gitlet/commit");
        boolean checker = false;
        String newID = "";
        for (File i: commit.listFiles()) {
            if (i.getName().substring(0, 6).equals(id.substring(0, 6))) {
                newID = i.getName();
                checker = true;
            }
        }
        if (!checker) {
            System.out.println("No commit with that id exists.");
            return;
        }
        File overwrite = Utils.join(commit, newID);
        Commit read = Utils.readObject(overwrite, Commit.class);
        HashMap<String, String> store = read.getBlob();
        boolean check = false;
        for (String i: store.keySet()) {
            if (i.equals(fileName)) {
                String s = store.get(i);
                Utils.writeContents(change, s);
                check = true;
            }
        }
        if (!check) {
            System.out.println("File does not exist in that commit.");
        }
    }

    /** Checkout a branch.
     * @param name branch name to checkout.
     * */
    public void checkoutBranch(String name) {
        File branch = new File(".gitlet/branch");
        File branches = Utils.join(branch, name);
        File check = Utils.join(branch, "*" + name);
        File head = new File(".gitlet/head/head");
        if (!branches.exists() && !check.exists()) {
            System.out.println("No such branch exists.");
            return;
        }
        if (check.exists()) {
            System.out.println("No need to checkout the current branch.");
            return;
        }
        File working = new File(".");
        Commit read = Utils.readObject(branches, Commit.class);
        Commit reads = Utils.readObject(head, Commit.class);
        HashMap<String, String> store = read.getBlob();
        HashMap<String, String> stores = reads.getBlob();
        for (String i: Utils.plainFilenamesIn(working)) {
            File currents = Utils.join(working, i);
            String temp = Utils.readContentsAsString(currents);
            if (!stores.containsKey(i)
                    && store.containsKey(i)
                    && !store.get(i).equals(temp)) {
                System.out.println("There is an untracked file in the way;"
                        + " delete it, or add and commit it first.");
                return;
            }
        }
        for (String i: store.keySet()) {
            File j = Utils.join(working, i);
            Utils.writeContents(j, store.get(i));
        }
        for (String i: stores.keySet()) {
            if (!store.containsKey(i)) {
                File j = Utils.join(working, i);
                j.delete();
            }
        }
        String current = "";
        for (File i: branch.listFiles()) {
            if (i.getName().startsWith("*")) {
                current = i.getName().substring(1);
                i.delete();
            }
        }
        File currently = Utils.join(branch, current);
        Utils.writeObject(currently, reads);
        branches.delete();
        Utils.writeObject(check, read);
        Utils.writeObject(head, read);
    }

    /** Checkout a branch.
     * @param name branch name.
     * */
    public void branch(String name) {
        File branch = new File(".gitlet/branch");
        File branches = Utils.join(branch, name);
        if (branches.exists()) {
            System.out.print("A branch with that name already exists.");
            return;
        }
        File head = new File(".gitlet/head/head");
        Commit read = Utils.readObject(head, Commit.class);
        Utils.writeObject(branches, read);
    }

    /** Remove a branch.
     * @param name branch name to remove.
     * */
    public void rmbranch(String name) {
        File branch = new File(".gitlet/branch");
        File branches = Utils.join(branch, name);
        File check = Utils.join(branch, "*" + name);
        if (!branches.exists() && !check.exists()) {
            System.out.println("A branch with that name does not exist.");
        } else if (check.exists()) {
            System.out.println("Cannot remove the current branch.");
        } else {
            branches.delete();
        }
    }

    /** Reset a commit with given id.
     * @param id commit id to reset.
     * */
    public void reset(String id) {
        File commit = new File(".gitlet/commit");
        boolean checker = false;
        String newID = "";
        for (File i: commit.listFiles()) {
            if (i.getName().substring(0, 6).equals(id.substring(0, 6))) {
                newID = i.getName();
                checker = true;
                break;
            }
        }
        if (!checker) {
            System.out.println("No commit with that id exists.");
            return;
        }
        File branch = new File(".gitlet/branch");
        File current = Utils.join(commit, newID);
        Commit read = Utils.readObject(current, Commit.class);
        HashMap<String, String> store = read.getBlob();
        File head = new File(".gitlet/head/head");
        Commit reads = Utils.readObject(head, Commit.class);
        HashMap<String, String> stores = reads.getBlob();
        File working = new File(".");
        for (String i: Utils.plainFilenamesIn(working)) {
            File currents = Utils.join(working, i);
            String temp = Utils.readContentsAsString(currents);
            if (!stores.containsKey(i) && store.containsKey(i)
                && !store.get(i).equals(temp)) {
                System.out.println("There is an untracked file in "
                        + "the way; delete it, or add and commit it first.");
                return;
            }
        }
        for (String i: Utils.plainFilenamesIn(working)) {
            if (store.containsKey(i)) {
                File k = Utils.join(working, i);
                Utils.writeContents(k, store.get(i));
            } else if (stores.containsKey(i)) {
                File j = Utils.join(working, i);
                j.delete();
            }
        }
        File stagingAdd = new File(".gitlet/staging/add");
        File stagingRemove = new File(".gitlet/staging/remove");
        for (File i: stagingAdd.listFiles()) {
            i.delete();
        }
        for (File i: stagingRemove.listFiles()) {
            i.delete();
        }
        String temp = "";
        for (File i: branch.listFiles()) {
            if (i.getName().startsWith("*")) {
                temp = i.getName();
            }
        }
        File currently = Utils.join(branch, temp);
        Utils.writeObject(currently, read);
        Utils.writeObject(head, read);
    }

    /** Merge into a branch.
     * @param name branch name to merge into.
     * */
    public void merge(String name) {
        boolean conflict = false;
        File stagingAdd = new File(".gitlet/staging/add");
        Commit split = splitPoint(name);
        File working = new File(".");
        HashMap<String, String> splitContent = split.getBlob();
        File branch = new File(".gitlet/branch");
        File branches = Utils.join(branch, name);
        Commit branching = Utils.readObject(branches, Commit.class);
        HashMap<String, String> branchContent = branching.getBlob();
        File head = new File(".gitlet/head/head");
        Commit heads = Utils.readObject(head, Commit.class);
        HashMap<String, String> headContent = heads.getBlob();
        for (String j: branchContent.keySet()) {
            if (!splitContent.containsKey(j) && !headContent.containsKey(j)) {
                checkoutOverwrite(branching.toString(), j);
                File stage = Utils.join(stagingAdd, j);
                Utils.writeContents(stage, branchContent.get(j));
            } else if (!splitContent.containsKey(j)
                    && headContent.containsKey(j)
                    && !headContent.get(j).equals(branchContent.get(j))) {
                conflict(j, headContent, branchContent);
                conflict = true;
            }
        }
        for (String i: splitContent.keySet()) {
            if (headContent.containsKey(i)
                    && branchContent.containsKey(i)) {
                if (headContent.get(i).equals(splitContent.get(i))
                    && !branchContent.get(i).equals(splitContent.get(i))) {
                    File work = Utils.join(working, i);
                    Utils.writeContents(work, branchContent.get(i));
                    File stage = Utils.join(stagingAdd, i);
                    Utils.writeContents(stage, branchContent.get(i));
                } else if (!headContent.get(i).equals(splitContent.get(i))
                        && !branchContent.get(i).equals(splitContent.get(i))
                        && !branchContent.get(i).equals(headContent.get(i))) {
                    conflict(i, headContent, branchContent);
                    conflict = true;
                }
            } else if (headContent.containsKey(i)
                    && !branchContent.containsKey(i)) {
                if (headContent.get(i).equals(splitContent.get(i))) {
                    rm(i);
                } else if (!headContent.get(i).equals(splitContent.get(i))) {
                    conflict(i, headContent, branchContent);
                    conflict = true;
                }
            }
        }
        commits("Merged " + name + " into " + currentBranch() + ".", branching);
        if (conflict) {
            System.out.println("Encountered a merge conflict.");
        }
    }

    /** Return current branch.
     * @return current branch name.
     * */
    public String currentBranch() {
        String temp = "";
        File branch = new File(".gitlet/branch");
        for (File i: branch.listFiles()) {
            if (i.getName().startsWith("*")) {
                temp = i.getName().substring(1);
            }
        }
        return temp;
    }

    /** Check merge failure.
     * @param name branch name to merge into.
     * */
    public void mergeFailure(String name) {
        File stagingAdd = new File(".gitlet/staging/add");
        File stagingRemove = new File(".gitlet/staging/remove");
        if (stagingAdd.listFiles().length != 0
                || stagingRemove.list().length != 0) {
            System.out.println("You have uncommitted changes.");
            return;
        }
        File branch = new File(".gitlet/branch");
        boolean exist = false;
        String current = "";
        for (File i: branch.listFiles()) {
            if (i.getName().startsWith("*")) {
                current = i.getName().substring(1);
            } else if (i.getName().equals(name)) {
                exist = true;
            }
        }
        if (name.equals(current)) {
            System.out.println("Cannot merge a branch with itself.");
            return;
        }
        if (!exist) {
            System.out.println("A branch with that name does not exist.");
            return;
        }
        File working = new File(".");
        File branching = Utils.join(branch, name);
        File head = new File(".gitlet/head/head");
        Commit read = Utils.readObject(branching, Commit.class);
        Commit reads = Utils.readObject(head, Commit.class);
        HashMap<String, String> store = read.getBlob();
        HashMap<String, String> stores = reads.getBlob();
        for (String i: Utils.plainFilenamesIn(working)) {
            File currents = Utils.join(working, i);
            String temp = Utils.readContentsAsString(currents);
            if (!stores.containsKey(i) && store.containsKey(i)
                    && !store.get(i).equals(temp)) {
                System.out.println("There is an untracked file in "
                        + "the way; delete it, or add and commit it first.");
                return;
            }
        }
        Commit split = splitPoint(name);
        File branches = Utils.join(branch, name);
        Commit branchings = Utils.readObject(branches, Commit.class);
        Commit heads = Utils.readObject(head, Commit.class);
        if (split.equals(branchings)) {
            System.out.println("Given branch is an"
                    + " ancestor of the current branch.");
            return;
        }
        if (heads != null && heads.equals(split)) {
            checkoutBranch(name);
            System.out.println("Current branch fast-forwarded.");
            return;
        }
        merge(name);
    }

    /** Write the conflicted file.
     * @param name branch name to merge into.
     * @param current content of head commit.
     * @param given content of given branch commit.
     * */
    public void conflict(String name, HashMap<String, String> current,
                         HashMap<String, String> given) {
        File track = new File(".");
        File conflictFile = Utils.join(track, name);
        String first = "<<<<<<< HEAD\n";
        String second = "";
        if (current.get(name) == null) {
            second = "";
        } else {
            second = current.get(name);
        }
        String third = "=======\n";
        String fourth = "";
        if (given.get(name) == null) {
            fourth = "";
        } else {
            fourth = given.get(name);
        }
        String fifth = ">>>>>>>\n";
        String result = first + second + third + fourth + fifth;
        Utils.writeContents(conflictFile, result);
        File stagingAdd = new File(".gitlet/staging/add");
        File conflicted = Utils.join(stagingAdd, name);
        Utils.writeContents(conflicted, result);
    }

    /** The splitPoint of the merge method.
     * @param branchName name of the given branch to merge into.
     * @return return the splitPoint of current branch and given branch.
     * */
    public Commit splitPoint(String branchName) {
        File branch = new File(".gitlet/branch");
        File branches = Utils.join(branch, branchName);
        Commit branching = Utils.readObject(branches, Commit.class);
        ArrayList<Commit> store = new ArrayList<>();
        store.add(branching);
        int index = 0;
        while (index < store.size()) {
            Commit temp = store.get(index);
            if (temp.getParent() != null
                    && !store.contains(temp.getParent())) {
                store.add(temp.getParent());
            }
            if (temp.getSecondParent() != null
                    && !store.contains(temp.getSecondParent())) {
                store.add(temp.getSecondParent());
            }
            index++;
        }
        HashSet<Commit> set = new HashSet<>(store);
        ArrayList<Commit> path = new ArrayList<>();
        File head = new File(".gitlet/head/head");
        Commit heads = Utils.readObject(head, Commit.class);
        path.add(heads);
        int number = 0;
        while (number < path.size()) {
            Commit temp = path.get(number);
            if (set.contains(temp)) {
                return temp;
            } else {
                if (temp.getParent() != null
                        && !path.contains(temp.getParent())) {
                    path.add(temp.getParent());
                }
                if (temp.getSecondParent() != null
                        && !path.contains(temp.getSecondParent())) {
                    path.add(temp.getSecondParent());
                }
            }
            number++;
        }
        return store.get(store.size() - 1);
    }

    /** Do addRemote command.
     * @param name remote name.
     * @param directory name of remote directory.
     * */
    public void addRemote(String name, String directory) {
        File remote = new File(".gitlet", "remote+" + name);
        if (remote.exists()) {
            System.out.println("A remote with that name already exists.");
            return;
        }
        Utils.writeContents(remote, directory);
    }

    /** Do rmRemote command.
     * @param name remote name.
     * */
    public void rmRemote(String name) {
        File remote = new File(".gitlet", "remote+" + name);
        if (!remote.exists()) {
            System.out.println("A remote with that name does not exist.");
            return;
        }
        remote.delete();

    }

    /** Do push command.
     * @param name remote name.
     * @param branch remote branch name.
     * */
    public void push(String name, String branch) {
        File remote = new File(".gitlet", "remote+" + name);
        String remoteDir = Utils.readContentsAsString(remote);
        File exist = new File(remoteDir);
        if (!exist.exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        File remoteHead = new File(remoteDir + "/head/head");
        File remoteCommit = new File(remoteDir + "/commit");
        File remoteBranch = new File(remoteDir + "/branch/" + branch);
        Commit remoteHeads = Utils.readObject(remoteHead, Commit.class);
        File head = new File(".gitlet/head/head");
        Commit heads = Utils.readObject(head, Commit.class);
        ArrayList<Commit> store = new ArrayList<>();
        store.add(heads);
        int index = 0;
        while (index < store.size()) {
            Commit temp = store.get(index);
            if (temp.getParent() != null
                    && !store.contains(temp.getParent())) {
                store.add(temp.getParent());
            }
            if (temp.getSecondParent() != null
                    && !store.contains(temp.getSecondParent())) {
                store.add(temp.getSecondParent());
            }
            index++;
        }
        if (!store.contains(remoteHeads)) {
            System.out.println("Please pull down"
                    + " remote changes before pushing.");
            return;
        }
        Utils.writeObject(remoteHead, heads);
        Utils.writeObject(remoteBranch, heads);
        for (Commit i: store) {
            File j = Utils.join(remoteCommit, i.toString());
            Utils.writeObject(j, i);
        }
    }

    /** Do fetch command.
     * @param name remote name.
     * @param branch remote branch name.
     * */
    public void fetch(String name, String branch) {
        File remote = new File(".gitlet", "remote+" + name);
        String remoteDir = Utils.readContentsAsString(remote);
        File exist = new File(remoteDir);
        if (!exist.exists()) {
            System.out.println("Remote directory not found.");
            return;
        }
        File remoteBranch = new File(remoteDir, "branch");
        File check = Utils.join(remoteBranch, "*" + branch);
        if (!check.exists()) {
            System.out.println("That remote does not have that branch.");
            return;
        }
        Commit current = Utils.readObject(check, Commit.class);
        File head = new File(".gitlet/head/head");
        Commit temp = current;
        File commit = new File(".gitlet/commit");
        ArrayList<Commit> store = new ArrayList<>();
        store.add(temp);
        int index = 0;
        while (index < store.size()) {
            temp = store.get(index);
            if (temp.getParent() != null
                    && !store.contains(temp.getParent())) {
                store.add(temp.getParent());
            }
            if (temp.getSecondParent() != null
                    && !store.contains(temp.getSecondParent())) {
                store.add(temp.getSecondParent());
            }
            index++;
        }
        for (Commit i: store) {
            File j = Utils.join(commit, i.toString());
            Utils.writeObject(j, i);
        }
        String add = name + "+" + branch;
        File added = Utils.join(".gitlet/branch", add);
        Utils.writeObject(added, current);
        Utils.writeObject(head, current);
    }

    /** Do pull command.
     * @param name remote name.
     * @param branch remote branch name.
     * */
    public void pull(String name, String branch) {
        String temp = name + "/" + branch;
        fetch(name, branch);
        mergeFailure(temp);
    }

}
