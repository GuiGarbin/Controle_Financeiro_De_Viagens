package org.example.users.register;

import org.example.users.User;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/auth")
public class RegisterController {

    // Spring injeta o serviço de cadastro automaticamente.
    private final UserRegisterService userRegisterService;

    public RegisterController(UserRegisterService userRegisterService) {
        this.userRegisterService = userRegisterService;
    }

    // POST /api/auth/register
    // Recebe: { "fullName": "...", "birthDate": "aaaa-mm-dd", "email": "...", "password": "..." }
    // Retorna: { "success": true } ou { "success": false, "message": "..." }
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterRequest request) {
        LocalDate birthDate;
        try {
            // O input de data do HTML envia no formato ISO (aaaa-mm-dd).
            birthDate = LocalDate.parse(request.birthDate());
        } catch (Exception e) {
            return ResponseEntity.status(400)
                    .body(new RegisterResponse(false, "Data de nascimento inválida."));
        }

        User created = userRegisterService.register(
                request.fullName(), birthDate, request.email(), request.password());

        // register() retorna null quando o email é inválido ou já está cadastrado.
        if (created == null) {
            return ResponseEntity.status(409)
                    .body(new RegisterResponse(false, "Email inválido ou já cadastrado."));
        }

        return ResponseEntity.ok(new RegisterResponse(true, "Conta criada com sucesso!"));
    }

    record RegisterRequest(String fullName, String birthDate, String email, String password) {}
    record RegisterResponse(boolean success, String message) {}
}
