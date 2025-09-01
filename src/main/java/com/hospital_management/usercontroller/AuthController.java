package com.hospital_management.usercontroller;


import com.hospital_management.config.JwtUtils;
import com.hospital_management.config.UserPrincipal;
import com.hospital_management.dtos.*;
import com.hospital_management.services.security.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final AuthService authService;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Authentication attempt for user: {}", loginRequest.getUsername());

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserPrincipal userDetails = (UserPrincipal) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());

            String refreshToken = jwtUtils.generateRefreshToken(userDetails.getUsername(), userDetails.getId());

            // Record successful login
            authService.recordSuccessfulLogin(userDetails.getUsername());

            log.info("User {} authenticated successfully", loginRequest.getUsername());

            return ResponseEntity.ok(new JwtResponse(
                    jwt,
                    refreshToken,
                    userDetails.getId(),
                    userDetails.getUsername(),
                    userDetails.getEmail(),
                    userDetails.getFirstName(),
                    userDetails.getLastName(),
                    roles
            ));

        } catch (Exception e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername(), e);

            // Record failed login
            authService.recordFailedLogin(loginRequest.getUsername());

            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid username or password!"));
        }
    }

    @PostMapping("/refreshtoken")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody TokenRefreshRequest request) {
        String requestRefreshToken = request.getRefreshToken();

        try {
            if (jwtUtils.validateJwtToken(requestRefreshToken) && jwtUtils.isRefreshToken(requestRefreshToken)) {
                String username = jwtUtils.getUserNameFromJwtToken(requestRefreshToken);
                Long userId = jwtUtils.getUserIdFromJwtToken(requestRefreshToken);
                String email = jwtUtils.getEmailFromJwtToken(requestRefreshToken);
                List<String> roles = jwtUtils.getRolesFromJwtToken(requestRefreshToken);

                String newAccessToken = jwtUtils.generateJwtToken(username, userId, email, roles);

                log.info("Token refreshed successfully for user: {}", username);

                return ResponseEntity.ok(new TokenRefreshResponse(newAccessToken, requestRefreshToken));
            } else {
                log.warn("Invalid refresh token provided");
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("Error: Invalid refresh token!"));
            }
        } catch (Exception e) {
            log.error("Error refreshing token: {}", e.getMessage());
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }

    @PostMapping("/signout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok(new MessageResponse("User logged out successfully!"));
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                if (jwtUtils.validateJwtToken(jwt)) {
                    String username = jwtUtils.getUserNameFromJwtToken(jwt);
                    Long userId = jwtUtils.getUserIdFromJwtToken(jwt);
                    String email = jwtUtils.getEmailFromJwtToken(jwt);
                    List<String> roles = jwtUtils.getRolesFromJwtToken(jwt);

                    return ResponseEntity.ok(new JwtResponse(
                            jwt, null, userId, username, email, null, null, roles
                    ));
                }
            }
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: Invalid token!"));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Error: " + e.getMessage()));
        }
    }
}
