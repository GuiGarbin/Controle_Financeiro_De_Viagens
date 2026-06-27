package org.example.users.auth;

import org.example.users.register.UserRegisterService;
import org.springframework.stereotype.Service;

@Service // tells Spring this is a service it should manage
public class AuthService {

    // O serviço de cadastro guarda os usuários registrados em memória.
    // O login usa essa mesma fonte para validar as credenciais.
    private final UserRegisterService userRegisterService;

    public AuthService(UserRegisterService userRegisterService) {
        this.userRegisterService = userRegisterService;
    }

    // Verifica se existe um usuário registrado com esse email e se a senha confere.
    public boolean checkCredentials(String email, String password) {
        return userRegisterService.findByEmail(email.toLowerCase())
                .map(user -> password.equals(user.getPassword()))
                .orElse(false);
    }
}
