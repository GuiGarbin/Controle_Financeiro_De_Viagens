package org.example.users.auth;

import org.example.users.User;
import org.example.users.register.UserRegisterService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service // tells Spring this is a service it should manage
public class AuthService {

    // O serviço de cadastro persiste os usuários (users.json).
    // O login usa essa mesma fonte para validar as credenciais.
    private final UserRegisterService userRegisterService;

    public AuthService(UserRegisterService userRegisterService) {
        this.userRegisterService = userRegisterService;
    }

    // Autentica: retorna o usuário se o email existir e a senha conferir,
    // ou Optional vazio caso contrário. O controller usa o id retornado para
    // que o frontend saiba de quem é a sessão (e filtre viagens por dono).
    public Optional<User> authenticate(String email, String password) {
        return userRegisterService.findByEmail(email.toLowerCase())
                .filter(user -> password.equals(user.getPassword()));
    }
}
