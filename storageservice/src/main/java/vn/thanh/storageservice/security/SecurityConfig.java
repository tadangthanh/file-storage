package vn.thanh.storageservice.security;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.*;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true, prePostEnabled = true)
public class SecurityConfig {
    private static final String GROUPS = "groups";
    private static final String REALM_ACCESS_CLAIM = "realm_access";
    private static final String ROLES_CLAIM = "roles";
    private final String[] WHITE_LIST = {
            "/api/storage/public/**",
            "/api/blob-events",
            "/api/v1/versions/view/**",
            "/actuator/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/api/v1/versions/*/download/*",

    };
    @Value("${spring.security.oauth2.resourceServer.jwt.jwk-set-uri}")
    private String jwkSetUri;

    // so với cấu hình trên method thì nó phải lọt qua đây trước
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF cho API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(WHITE_LIST).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter()))
//                        .bearerTokenResolver(new CustomBearerTokenResolver())
                );

        return http.build();
    }

    // Chuyển claim "roles" trong JWT thành GrantedAuthority
    // dùng cho Authorization Server tự code
//    private JwtAuthenticationConverter jwtAuthenticationConverter() {
//        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
//
//        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
//            List<GrantedAuthority> authorities = new ArrayList<>();
//
//            // Map roles → ROLE_*
//            List<String> roles = jwt.getClaimAsStringList("roles");
//            if (roles != null) {
//                authorities.addAll(roles.stream()
//                        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
//                        .toList());
//            }
//
//            // Map scope → SCOPE_*
//            List<String> scopes = jwt.getClaimAsStringList("scope"); // ✅ Dùng đúng kiểu
//            if (scopes != null) {
//                authorities.addAll(scopes.stream()
//                        .map(s -> new SimpleGrantedAuthority("SCOPE_" + s))
//                        .toList());
//            }
//
//            return authorities;
//        });
//
//        return converter;
//    }

    /// decode token o day https://jwt.io/
    /// // dung cho Keycloak
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Set<String> mappedAuthorities = new HashSet<>();

            if (jwt.hasClaim(REALM_ACCESS_CLAIM)) {
                List<String> roles = (List<String>) jwt.getClaimAsMap(REALM_ACCESS_CLAIM).get(ROLES_CLAIM);
                ///join list
                mappedAuthorities.addAll(roles);
            } else if (jwt.hasClaim(GROUPS)) {
                Collection<String> roles = jwt.getClaim(GROUPS);
                ///join list
                mappedAuthorities.addAll(roles);
            }

            // scope client
            List<String> scopes = Arrays.stream(jwt.getClaimAsString("scope").split("\\s+")).map(scope -> "SCOPE_" + scope).toList();

            mappedAuthorities.addAll(scopes);
            // make authorities
            return mappedAuthorities.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
        });

        return converter;
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }
}
