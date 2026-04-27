package com.fluxaj.service;

import com.fluxaj.model.Usuario;
import com.fluxaj.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
public class CustomOidcUserService extends OidcUserService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private TokenEncryptionService tokenEncryptionService;

    @Override
    public OidcUser loadUser(OidcUserRequest userRequest) throws OAuth2AuthenticationException {
        OidcUser oidcUser = super.loadUser(userRequest);

        String email = oidcUser.getEmail();
        String nombre = oidcUser.getFullName();

        // El access token que da Google para llamar Gmail API
        String rawAccessToken = userRequest.getAccessToken().getTokenValue();

        // Cifrarlo antes de guardar
        String encryptedToken = tokenEncryptionService.encrypt(rawAccessToken);

        usuarioRepository.findByEmail(email).ifPresentOrElse(
            usuario -> {
                // Actualizar token cifrado en cada login
                usuario.setAccessTokenCifrado(encryptedToken);
                usuarioRepository.save(usuario);
            },
            () -> {
                // Nuevo usuario
                Usuario nuevoUsuario = new Usuario();
                nuevoUsuario.setEmail(email);
                nuevoUsuario.setNombre(nombre);
                nuevoUsuario.setRol("USER");
                nuevoUsuario.setAccessTokenCifrado(encryptedToken);
                usuarioRepository.save(nuevoUsuario);
            }
        );

        return oidcUser;
    }
}