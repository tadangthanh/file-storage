package vn.thanh.storageservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import vn.thanh.storageservice.exception.InvalidTokenException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthenticationService {
    private final JwtDecoder jwtDecoder;
    public Authentication authenticateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token); // nếu decode fail sẽ ném exception
            String email = jwt.getClaimAsString("email");
            log.info("authenticated email: {}",email);
            List<GrantedAuthority> authorities = new ArrayList<>();
            // Lấy roles từ token
            Map<String, Object> realmAccess = jwt.getClaim("realm_access");
            if (realmAccess != null && realmAccess.containsKey("roles")) {
                List<String> roles = (List<String>) realmAccess.get("roles");
                for (String role : roles) {
                    authorities.add(new SimpleGrantedAuthority(role));
                }
            }

            UserDetails userDetails = new User(email, "", authorities);
            return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
        } catch (JwtException e) {
            throw new InvalidTokenException("Token không hợp lệ");
        }
    }
}
