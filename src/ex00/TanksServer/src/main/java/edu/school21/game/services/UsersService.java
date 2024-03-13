package edu.school21.game.services;

import edu.school21.game.models.User;

        import java.util.Optional;

public interface UsersService {
    User signUp(String name, String password);

    void updateUser(User user);

    boolean checkUser(String name, String password);

    Optional<User> getUserByName(String name);

    User getUserById(Long id);
}
