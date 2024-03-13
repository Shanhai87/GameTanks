package edu.school21.game.repositories;

import edu.school21.game.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;

@Component
public class UsersRepositoryImpl implements UsersRepository {
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UsersRepositoryImpl(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Override
    public Optional<User> findById(Long id) {
        List<User> users = jdbcTemplate.query("SELECT * FROM tanks.users WHERE id = ?",
                new BeanPropertyRowMapper<>(User.class), id);
        return Optional.ofNullable(users.isEmpty() ? null : users.getFirst());
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query("SELECT * FROM tanks.users", new BeanPropertyRowMapper<>(User.class));
    }

    @Override
    public void save(User entity) {
        jdbcTemplate.update("INSERT INTO tanks.users (name, password, status, hits, misses) VALUES (?, ?, ?, ?, ?)", entity.getName(),
                entity.getPassword(), entity.getStatus(), entity.getHits(), entity.getMisses());
    }

    @Override
    public void update(User entity) {
        jdbcTemplate.update("UPDATE tanks.users SET (name, password, status, hits, misses) = (?, ?, ?, ?, ?) WHERE id = ?", entity.getName(),
                entity.getPassword(), entity.getStatus(), entity.getHits(), entity.getMisses(), entity.getId());
    }

    @Override
    public void delete(Long id) {
        jdbcTemplate.update("DELETE FROM tanks.users WHERE id = ?", id);
    }

    @Override
    public Optional<User> findByName(String name) {
        List<User> users = jdbcTemplate.query("SELECT * FROM tanks.users WHERE name = ?",
                new BeanPropertyRowMapper<>(User.class), name);
        return Optional.ofNullable(users.isEmpty() ? null : users.getFirst());
    }
}
