package dev.emreozcan.pazarsync.orderservice.security;

import dev.emreozcan.pazarsync.orderservice.domain.UserAccount;
import dev.emreozcan.pazarsync.orderservice.domain.UserAccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@ConditionalOnProperty(prefix = "pazarsync.security", name = "seed-users", havingValue = "true")
public class UserSeeder implements ApplicationRunner {

    private final UserAccountRepository users;
    private final PasswordEncoder passwordEncoder;
    private final SecurityProperties properties;

    public UserSeeder(UserAccountRepository users, PasswordEncoder passwordEncoder, SecurityProperties properties) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        for (SecurityProperties.SeedUser seed : properties.getUsers()) {
            if (users.existsByUsername(seed.getUsername())) {
                continue;
            }
            UserAccount user = new UserAccount();
            user.setId(UUID.randomUUID());
            user.setUsername(seed.getUsername());
            user.setPasswordHash(passwordEncoder.encode(seed.getPassword()));
            user.setRole(seed.getRole());
            users.save(user);
        }
    }
}
