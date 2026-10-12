package dev.emreozcan.pazarsync.orderservice.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@ConfigurationProperties(prefix = "pazarsync.security")
public class SecurityProperties {

    private String jwtSecret = "pazarsync-dev-jwt-secret-32bytes";
    private boolean seedUsers;
    private List<SeedUser> users = new ArrayList<>();

    public String getJwtSecret() {
        return jwtSecret;
    }

    public void setJwtSecret(String jwtSecret) {
        this.jwtSecret = jwtSecret;
    }

    public boolean isSeedUsers() {
        return seedUsers;
    }

    public void setSeedUsers(boolean seedUsers) {
        this.seedUsers = seedUsers;
    }

    public List<SeedUser> getUsers() {
        return users;
    }

    public void setUsers(List<SeedUser> users) {
        this.users = users;
    }

    public static class SeedUser {
        private String username;
        private String password;
        private String role;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }
    }
}
