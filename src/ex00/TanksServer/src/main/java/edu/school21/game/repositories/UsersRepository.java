package edu.school21.game.repositories;

import edu.school21.game.models.User;

import java.util.Optional;

public interface UsersRepository extends CrudRepository<User> {
    Optional<User> findByName(String name);
}
