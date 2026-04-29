package org.example.users.auth;

import org.springframework.stereotype.Service;

@Service // tells Spring this is a service it should manage
public class AuthService {

    // Hardcoded credentials for now.
    // Later you will check against a database.
    public boolean checkCredentials(String email, String password) {
        return email.equals("admin@test.com") && password.equals("123456");
    }
}