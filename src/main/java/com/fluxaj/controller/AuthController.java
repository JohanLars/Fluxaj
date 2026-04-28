package com.fluxaj.controller;

import com.fluxaj.model.Usuario;
import com.fluxaj.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
public class AuthController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password) {

        Usuario u = new Usuario();
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));

        repo.save(u);

        return "Usuario creado";
    }
}