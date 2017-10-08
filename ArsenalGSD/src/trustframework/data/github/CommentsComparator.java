/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.data.github;

import java.util.Comparator;
import org.eclipse.egit.github.core.Comment;

/**
 * Comparator to compare comments by date.
 * @author Guilherme
 */
public class CommentsComparator implements Comparator<Comment>{

    @Override
    public int compare(Comment o1, Comment o2) {
        return o1.getUpdatedAt().compareTo(o2.getUpdatedAt());
    }
    
}
