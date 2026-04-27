package com.fluxaj.repository; 

import com.fluxaj.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    // Busca el correo de Google Iniciar Sesion
    Optional<Usuario> findByEmail(String email);
}
