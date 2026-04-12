package com.acuityspace.mantle;

import com.acuityspace.mantle.config.TestContainersBase;
import com.acuityspace.mantle.domain.model.User;
import com.acuityspace.mantle.domain.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class UserRepositoryTest extends TestContainersBase {

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindById() {
        User user = new User("alice@example.com", "hashed_pw", "Alice");
        User saved = userRepository.save(user);

        Optional<User> found = userRepository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("alice@example.com");
        assertThat(found.get().getPasswordHash()).isEqualTo("hashed_pw");
        assertThat(found.get().getName()).isEqualTo("Alice");
        assertThat(found.get().getCreatedAt()).isNotNull();
    }

    @Test
    void findByEmail() {
        User user = new User("bob@example.com", "hashed_pw", "Bob");
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("bob@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Bob");
    }

    @Test
    void existsByEmail_true() {
        User user = new User("carol@example.com", "hashed_pw", "Carol");
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("carol@example.com")).isTrue();
    }

    @Test
    void existsByEmail_false() {
        assertThat(userRepository.existsByEmail("nobody@example.com")).isFalse();
    }

    @Test
    void emailUniqueness() {
        userRepository.saveAndFlush(new User("dup@example.com", "hashed_pw", "First"));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(new User("dup@example.com", "hashed_pw2", "Second"))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
