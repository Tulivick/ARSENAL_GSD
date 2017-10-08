/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.data.github;

import java.util.Comparator;
import org.eclipse.egit.github.core.IssueEvent;

/**
 * Comparator that compares IssueEvents by date in decrescent order.
 * @author guilherme
 */
public class IssueEventComparator implements Comparator<IssueEvent>{

    @Override
    public int compare(IssueEvent o1, IssueEvent o2) {
        return o2.getCreatedAt().compareTo(o1.getCreatedAt());
    }
    
}
