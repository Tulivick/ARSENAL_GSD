/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.data.github;

import java.util.Date;
import org.eclipse.egit.github.core.User;

/**
 *
 * @author Guilherme
 */
public class GitComment implements Comparable<GitComment>{
    private String body;
    private Date updatedAt;
    private User creator;
    private Long id;

    public GitComment(String body, Date updatedAt, User creator, Long id) {
        this.body = body;
        this.updatedAt = updatedAt;
        this.creator = creator;
        this.id = id;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public int compareTo(GitComment o) {
        return this.getUpdatedAt().compareTo(o.getUpdatedAt());
    }
    
}
