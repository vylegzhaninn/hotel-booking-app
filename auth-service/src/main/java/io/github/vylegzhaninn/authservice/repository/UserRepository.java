package io.github.vylegzhaninn.authservice.repository;

import io.github.vylegzhaninn.authservice.model.User;
import org.jdbi.v3.sqlobject.config.RegisterConstructorMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

@RegisterConstructorMapper(User.class)
public interface UserRepository {

    @SqlQuery("SELECT id, name, password, role FROM users WHERE name = :name")
    Optional<User> findByName(@Bind("name") String name);

    @SqlUpdate("INSERT INTO users (name, password, role) VALUES (:name, :password, :role)")
    void save(@Bind("name") String name, @Bind("password") String password, @Bind("role") String role);
}
