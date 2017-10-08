/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.data.github;

import java.util.Objects;
import org.eclipse.egit.github.core.User;

/**
 * Adaptor for class User from GitHub API. This adaptor implements method equals in order to use it in Sets.
 * @author Guilherme
 */
public class UserAdaptor {
    private final User user;

    public UserAdaptor(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean equals(Object obj) {
        return user.getLogin().equals(obj);
        }

}
