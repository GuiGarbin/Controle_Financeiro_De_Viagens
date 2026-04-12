package org.example.auth;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Spring automatically creates and injects the AuthService here.
    // You never call 'new AuthService()' yourself.
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/login
    // Receives: { "email": "...", "password": "..." }
    // Returns:  { "success": true } or { "success": false, "message": "..." }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        boolean valid = authService.checkCredentials(request.email(), request.password());

        if (valid) {
            return ResponseEntity.ok(new LoginResponse(true, "Login efetuado com sucesso!"));
        } else {
            // 401 = Unauthorized
            return ResponseEntity.status(401).body(new LoginResponse(false, "Credenciais inválidas!"));
        }
    }

    // These are the shapes of the JSON going in and coming out.
    // 'record' is a shorthand in modern Java for a simple data class.
    record LoginRequest(String email, String password) {}
    record LoginResponse(boolean success, String message) {}
}