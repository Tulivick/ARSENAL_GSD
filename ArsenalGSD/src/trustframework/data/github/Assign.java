/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.data.github;

import java.util.Date;
import org.eclipse.egit.github.core.User;

/**
 * This class stores assigner data. It contais the assigner login and when he assigned.
 * @author guilherme
 */
public class Assign {
    private final User user;
    private final Date date;

    public Assign(User user, Date date) {
        this.user = user;
        this.date = date;
    }

    public User getUser() {
        return user;
    }

    public Date getDate() {
        return date;
    }
}
