package com.studycoachai.security;

import java.time.Instant;

import com.studycoachai.entity.User;
import com.studycoachai.exception.AppConfigurationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.JwtEncodingException;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;
    private final long expiresInSeconds;

    public JwtService(
            JwtEncoder jwtEncoder,
            @Value("${app.jwt.expires-in-seconds:86400}") long expiresInSeconds
    ) {
        this.jwtEncoder = jwtEncoder;
        this.expiresInSeconds = expiresInSeconds;
    }

    public String generateToken(User user) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("study-coach-ai")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expiresInSeconds))
                .subject(user.getId().toString())
                .claim("username", user.getUsername())
                .claim("email", user.getEmail())
                .build();
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        try {
            return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
        } catch (JwtEncodingException exception) {
            throw new AppConfigurationException(
                    "JWT token generation failed. Check that JWT_SECRET is set and is at least 32 characters long.",
                    exception
            );
        }
    }
}
