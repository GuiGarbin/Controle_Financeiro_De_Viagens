package org.example.users.register;

import org.example.repository.UserRepository;
import org.example.users.User;
import org.example.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class UserRegisterService {

    // Persistência em arquivo (users.json) via UserRepository. Os usuários
    // sobrevivem a reinícios da aplicação, ao contrário da antiga lista em memória.
    private final UserRepository userRepository;

    public UserRegisterService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

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

        // Salva o usuário no arquivo (users.json).
        userRepository.save(newUser);

        return newUser;
    }

    // Verifica se já existe um usuário cadastrado com o email informado.
    private boolean emailAlreadyExists(String userEmail){
        return findByEmail(userEmail).isPresent();
    }

    // Retorna a lista de usuários registrados (lidos do arquivo).
    public List<User> getRegisteredUsers(){
        return userRepository.findAll();
    }

    // Procura um usuário registrado pelo email (usado pelo login).
    // Normaliza para minúsculas porque os emails são gravados nesse formato.
    public Optional<User> findByEmail(String email){
        if (email == null) {
            return Optional.empty();
        }
        return userRepository.findByEmail(email.toLowerCase());
    }

    private boolean checkValidEmail(String userEmail){
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        return userEmail.matches(regexPattern);
    }
}
