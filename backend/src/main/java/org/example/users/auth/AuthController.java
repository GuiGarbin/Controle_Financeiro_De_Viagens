package org.example.users.auth;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Spring vai criar uma instância de AuthService e injetar aqui automaticamente.
    // você não precisa se preocupar com isso, apenas declare a dependência no construtor.
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // POST /api/auth/login
    // Recebe: { "email": "...", "password": "..." }
    // Returna:  { "success": true } ou { "success": false, "message": "..." }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        boolean valid = authService.checkCredentials(request.email(), request.password());

        if (valid) {
            return ResponseEntity.ok(new LoginResponse(true, "Login efetuado com sucesso!"));
        } else {
            // 401 = Login incorreto == não autorizado
            return ResponseEntity.status(401).body(new LoginResponse(false, "Credenciais inválidas!"));
        }
    }


    record LoginRequest(String email, String password) {}
    record LoginResponse(boolean success, String message) {}
}