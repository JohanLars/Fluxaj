package com.fluxaj.service;

import com.fluxaj.model.Usuario;
import com.fluxaj.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UsuarioRepository repo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Usuario u = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no existe"));

        return User.builder()
                .username(u.getEmail())
                .password(u.getPassword())
                .roles("USER")
                .build();
    }
}