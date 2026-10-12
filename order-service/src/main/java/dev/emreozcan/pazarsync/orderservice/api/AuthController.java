package dev.emreozcan.pazarsync.orderservice.api;

import dev.emreozcan.pazarsync.orderservice.domain.UserAccountRepository;
import dev.emreozcan.pazarsync.orderservice.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserAccountRepository users;
    private final JwtService jwtService;

    public AuthController(
            AuthenticationManager authenticationManager,
            UserAccountRepository users,
            JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.users = users;
        this.jwtService = jwtService;
    }

    @PostMapping("/api/auth/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        return users.findByUsername(request.username())
                .map(user -> new LoginResponse(jwtService.issue(user), user.getUsername(), user.getRole()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    }

    public record LoginRequest(String username, String password) {
    }

    public record LoginResponse(String token, String username, String role) {
    }
}
