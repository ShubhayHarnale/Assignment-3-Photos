package photos.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhotoLibrary implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String ADMIN_USERNAME = "admin";
    public static final String STOCK_USERNAME = "stock";

    private final List<User> users;

    public PhotoLibrary() {
        this.users = new ArrayList<>();
        ensureStockUser();
    }

    public User ensureStockUser() {
        User stockUser = getUser(STOCK_USERNAME);
        if (stockUser == null) {
            stockUser = new User(STOCK_USERNAME);
            users.add(stockUser);
        }
        return stockUser;
    }

    public List<User> getUsers() {
        List<User> sortedUsers = new ArrayList<>(users);
        Collections.sort(sortedUsers);
        return Collections.unmodifiableList(sortedUsers);
    }

    public boolean hasUser(String username) {
        return getUser(username) != null;
    }

    public User getUser(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    public boolean addUser(User user) {
        if (user == null) {
            return false;
        }

        String username = user.getUsername();
        if (username.equalsIgnoreCase(ADMIN_USERNAME)
                || username.equalsIgnoreCase(STOCK_USERNAME)
                || hasUser(username)) {
            return false;
        }

        users.add(user);
        return true;
    }

    public boolean removeUser(String username) {
        User user = getUser(username);
        if (user == null || username.equalsIgnoreCase(STOCK_USERNAME)) {
            return false;
        }
        return users.remove(user);
    }
}
