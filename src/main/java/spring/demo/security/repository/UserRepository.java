package spring.demo.security.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import spring.demo.security.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {


    public User findByUsername(String username);

    public User findByEmail(String email);
}
