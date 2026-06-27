package org.example.users.register;

import org.example.users.User;
import org.example.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserRegisterService {

    // Armazenamento em memória: os usuários registrados ficam guardados nesta
    // lista enquanto a aplicação estiver rodando. Por enquanto não salvamos em
    // arquivo no computador, tudo fica apenas na memória.
    private final List<User> registeredUsers = new ArrayList<>();

    // Registra um novo usuário a partir dos dados informados.
    // Retorna o usuário criado em caso de sucesso, ou null se os dados forem
    // inválidos (email mal formatado ou já cadastrado).
    public User register(String fullName, LocalDate birthDate, String userEmail, String userPassword){
        String email = userEmail.toLowerCase();

        // Valida o formato do email antes de criar qualquer coisa.
        if (!checkValidEmail(email)){
            return null; // email inválido, não registra
        }

        // Impede o cadastro de dois usuários com o mesmo email.
        if (emailAlreadyExists(email)){
            return null; // email já cadastrado
        }

        // Cria o usuário e preenche todos os campos.
        User newUser = new User(fullName, birthDate);
        newUser.setId(IdGenerator.forUser()); // gera um id único (ex: usr_a1b2c3d4)
        newUser.setEmail(email);
        newUser.setPassword(userPassword);

        // Salva o usuário na memória.
        registeredUsers.add(newUser);

        return newUser;
    }

    // Verifica se já existe um usuário cadastrado com o email informado.
    private boolean emailAlreadyExists(String userEmail){
        return registeredUsers.stream()
                .anyMatch(u -> userEmail.equalsIgnoreCase(u.getEmail()));
    }

    // Retorna a lista de usuários registrados em memória.
    public List<User> getRegisteredUsers(){
        return registeredUsers;
    }

    // Procura um usuário registrado pelo email (usado pelo login).
    public Optional<User> findByEmail(String email){
        return registeredUsers.stream()
                .filter(u -> u.getEmail() != null && u.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    private boolean checkValidEmail(String userEmail){
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        return userEmail.matches(regexPattern);
    }
}
