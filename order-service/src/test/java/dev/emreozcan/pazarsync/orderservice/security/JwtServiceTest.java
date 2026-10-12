package dev.emreozcan.pazarsync.orderservice.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import dev.emreozcan.pazarsync.orderservice.domain.UserAccount;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JwtServiceTest {

    @Test
    void issuesATokenThatCarriesTheRole() {
        SecretKey key = new SecretKeySpec("pazarsync-dev-jwt-secret-32bytes".getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        UserAccount user = new UserAccount();
        user.setUsername("ada");
        user.setRole("ADMIN");

        Jwt jwt = decoder.decode(new JwtService(encoder).issue(user));

        assertEquals("ada", jwt.getSubject());
        assertEquals("ADMIN", jwt.getClaim("role"));
    }
}
