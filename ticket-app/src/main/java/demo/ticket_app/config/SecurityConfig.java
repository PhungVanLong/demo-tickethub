package demo.ticket_app.config;

import java.util.Arrays;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import demo.ticket_app.service.ApplicationUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ApplicationUserDetailsService userDetailsService;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            ApplicationUserDetailsService userDetailsService
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth
                // Public
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/health", "/api/docs").permitAll()
                .requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/published").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/categories").permitAll()
                    .requestMatchers(new RegexRequestMatcher("^/api/events/[0-9]+$", "GET")).permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/search").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/city/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/*/stats/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/events/stats/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/checkout/events/*/tiers").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/checkout/payments/webhook/**").permitAll()
                // Admin only
                .requestMatchers(HttpMethod.GET, "/api/events/pending").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/events/*/approve").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/events/*/reject").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users/*/promote-organizer").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users/*/demote-customer").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users/*/activate").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/users/*/deactivate").hasRole("ADMIN")
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/stats/platform").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/stats/organizer/**").hasAnyRole("ORGANIZER", "ADMIN")
                // Customer/Organizer/Admin can submit event idea
                .requestMatchers(HttpMethod.POST, "/api/events").hasAnyRole("CUSTOMER", "ORGANIZER", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/events/**").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/events/**").hasAnyRole("ORGANIZER", "ADMIN")
                .requestMatchers("/api/events/*/seat-maps/**").hasAnyRole("ORGANIZER", "ADMIN")
                // Any authenticated user
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:5173", "http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
