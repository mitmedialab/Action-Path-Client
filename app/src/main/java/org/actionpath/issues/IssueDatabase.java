package org.actionpath.issues;

import java.util.HashMap;

/**
 * Acts as a singleton wrapper around list of issues.  Call getInstance() to get one.
 * Created by rahulb on 5/26/15.
 */
public class IssueDatabase {

    public static IssueDatabase dbInstance = null;

    public static HashMap<Integer, Issue> issues = new HashMap<Integer, Issue>();

    private IssueDatabase(){
        issues = new HashMap<Integer, Issue>();
    }

    public static final IssueDatabase getInstance(){
        if(dbInstance==null){
            dbInstance = new IssueDatabase();
        }
        return dbInstance;
    }

    public void add(int id, Issue issue) {
        issues.put(id,issue);
    }

    public static Issue get(int id){
        IssueDatabase issueDB = getInstance();
        return issueDB.issues.get(id);
    }

}
