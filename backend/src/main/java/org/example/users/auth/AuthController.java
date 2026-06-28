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
        // Em caso de sucesso, devolve o id do usuário para o frontend manter a
        // sessão e enviá-lo (header X-User-Id) ao buscar/criar viagens.
        return authService.authenticate(request.email(), request.password())
                .map(user -> ResponseEntity.ok(
                        new LoginResponse(true, "Login efetuado com sucesso!", user.getId())))
                .orElseGet(() -> ResponseEntity.status(401) // 401 = não autorizado
                        .body(new LoginResponse(false, "Credenciais inválidas!", null)));
    }


    record LoginRequest(String email, String password) {}
    record LoginResponse(boolean success, String message, String userId) {}
}