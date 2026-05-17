package org.example.users.register;

import org.example.users.User;
import org.springframework.stereotype.Service;
import java.time.LocalDate;

@Service
public class UserRegisterService {

    public void UserRegisterService(String fullName, LocalDate birthDate, String userEmail, String userPassword){
        User newUser = new User(fullName, birthDate);

        if (checkValidEmail(userEmail.toLowerCase())){ // email validation
            newUser.setEmail(userEmail.toLowerCase());
        }


    }

    private boolean checkValidEmail(String userEmail){
        String regexPattern = "^(?=.{1,64}@)[A-Za-z0-9_-]+(\\.[A-Za-z0-9_-]+)*@" + "[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";

        return userEmail.matches(regexPattern);
    }
}