package br.lab.testesubmissao.Controller;

import br.lab.testesubmissao.Dto.auth.AuthResponse;
import br.lab.testesubmissao.Dto.auth.LoginRequest;
import br.lab.testesubmissao.Dto.auth.RegisterRequest;
import br.lab.testesubmissao.Dto.auth.UserProfileResponse;
import br.lab.testesubmissao.Entity.Role;
import br.lab.testesubmissao.Entity.User;
import br.lab.testesubmissao.Security.JwtService;
import br.lab.testesubmissao.Service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthController(
            UserService userService,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.ROLE_USER);

        User created = userService.register(user);
       String token = jwtService.generateToken(created.getUsername(),created.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(
                created.getId(),
                created.getUsername(),
                created.getRole(),
                token
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userService.findByUsername(request.username());
        String token = jwtService.generateToken(user.getUsername(),user.getRole().name());
        return ResponseEntity.ok(new AuthResponse(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                token
        ));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> me(Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        return ResponseEntity.ok(new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRole()
        ));
    }
}
