package edu.school21.game.services;

import edu.school21.game.models.User;
import edu.school21.game.repositories.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class UsersServiceImpl implements UsersService {
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;

    @Autowired
    public UsersServiceImpl(PasswordEncoder passwordEncoder, UsersRepository usersRepository) {
        this.passwordEncoder = passwordEncoder;
        this.usersRepository = usersRepository;
    }

    @Override
    public User signUp(String name, String password) {
        if (usersRepository.findByName(name).isPresent()) {
            return null;
        }
        User user = new User(name);
        user.setPassword(passwordEncoder.encode(password));
        usersRepository.save(user);
        return user;
    }

    @Override
    public void updateUser(User user) {
        usersRepository.update(user);
    }

    @Override
    public Optional<User> getUserByName(String name) {
        return usersRepository.findByName(name);
    }

    @Override
    public boolean checkUser(String name, String password) {
        Optional<User> optionalUser = usersRepository.findByName(name);
        return optionalUser.isPresent() && passwordEncoder.matches(password, optionalUser.get().getPassword());
    }

    @Override
    public User getUserById(Long id) {
        Optional<User> optionalUser = usersRepository.findById(id);
        return optionalUser.orElse(null);
    }
}
