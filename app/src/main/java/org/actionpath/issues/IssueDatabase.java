package org.actionpath.issues;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Acts as a singleton wrapper around list of issues.  Call getInstance() to get one.
 * Created by rahulb on 5/26/15.
 */
public class IssueDatabase {

    public static IssueDatabase dbInstance = null;

    public static HashMap<Integer, Issue> issues = new HashMap<Integer, Issue>();

    private String CLASS_NAME = this.getClass().getName();

    private IssueDatabase(){
        issues = new HashMap<Integer, Issue>();
    }

    private void addTestIssues(){
        final double Cambridge_lat = 42.359254;
        final double Cambridge_long = -71.093667;
        final float Cambridge_rad = 1601;
        final double Cambridge_lat2 = 42.359255;
        final double Cambridge_long2 = -71.093666;
        Issue testIssue1 = new Issue(1234, "Acknowledged", "Toy Train Hack", "Giant Toy Train hack on Kendall Square T entrance.", Cambridge_lat, Cambridge_long, "350 Main Street, Cambridge, Massachusetts", "null", null, null, 9841);
        testIssue1.setTest(true);
        add(testIssue1);
        Issue testIssue2 = new Issue(2345, "Acknowledged", "Pothole", "Pothole on the corner of Mass Ave and Vassar.", Cambridge_lat, Cambridge_long, "Massachusetts Ave./Vassar St., Cambridge, Massachusetts", "null", null, null, 9841);
        testIssue2.setTest(true);
        add(testIssue2);
        Log.d(CLASS_NAME, "added test issues");
    }

    public static final IssueDatabase getInstance(){
        if(dbInstance==null){
            dbInstance = new IssueDatabase();
        }
        return dbInstance;
    }

    public void add(Issue issue) {
        issues.put(issue.getId(),issue);
    }

    public static Issue get(int id){
        IssueDatabase issueDB = getInstance();
        Issue issue = issueDB.issues.get(id);
        if(issue==null){
            Log.e("IssueDatabase","request for issue that doesn't exist: "+id);
        }
        return issue;
    }

    public Collection<Issue> getAll(){
        return issues.values();
    }

}
