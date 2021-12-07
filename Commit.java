package gitlet;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/** Commit class of commit.
 *  @author Zachary Zhang
 * */
public class Commit implements Serializable {

    /** log message of commit. */
    private String _logMessage;

    /** date of commit. */
    private String _date;

    /** first parent of commit. */
    private Commit _firstParent;

    /** second parent of commit. */
    private Commit _secondParent;

    /** content of commit. */
    private HashMap<String, String> _blob;

    /** Constructor of Commit.class.
     * @param logMessage log message of the commit.
     * @param firstParent first parent of commit.
     * @param secondParent second parent of commit.
     * @param blob content of commit.
     * */
    public Commit(String logMessage, Commit firstParent,
                  Commit secondParent, HashMap<String, String> blob) {
        _logMessage = logMessage;
        _firstParent = firstParent;
        _secondParent = secondParent;
        Timestamp time = new Timestamp(0);
        _blob = blob;
        SimpleDateFormat formatter = new
                SimpleDateFormat("EEE MMM d HH:mm:ss yyyy Z");
        Date date = new Date();
        if (firstParent == null) {
            date = new Date(time.getTime());
        }
        _date = formatter.format(date);
    }

    /** Return the parent of the commit.*/
    public Commit getParent() {
        return _firstParent;
    }

    @Override
    public boolean equals(Object x) {
        return x.toString().equals(toString());
    }

    /** Return the second parent of the commit.*/
    public Commit getSecondParent() {
        return _secondParent;
    }

    /** Return the date of the commit.*/
    public String getDate() {
        return _date;
    }

    /** Return the message of the commit.*/
    public String getMessage() {
        return _logMessage;
    }

    /** Return the content of the commit.*/
    public HashMap<String, String> getBlob() {
        return _blob;
    }

    @Override
    public int hashCode() {
        return Integer.getInteger(toString(), 16);
    }

    @Override
    public String toString() {
        ArrayList<Object> temp = new ArrayList<>();
        temp.add(_logMessage);
        temp.add(_date);
        for (String i: _blob.values()) {
            temp.add(i);
        }
        return Utils.sha1(temp);
    }





}
