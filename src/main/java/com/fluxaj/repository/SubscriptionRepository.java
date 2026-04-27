package com.fluxaj.repository;

import com.fluxaj.model.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    
    // Consulta por ID del usuario, suscripciones asociadas
    @Query("SELECT s FROM Subscription s WHERE s.usuario.id = :usuarioId")
    List<Subscription> findByUsuarioId(@Param("usuarioId") Long usuarioId);
}