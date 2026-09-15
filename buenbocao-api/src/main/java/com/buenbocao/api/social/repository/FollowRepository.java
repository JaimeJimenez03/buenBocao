package com.buenbocao.api.social.repository;

import com.buenbocao.api.social.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repositorio para la entidad Follow.
 */
@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId);

    boolean existsByFollowerIdAndFollowedId(Long followerId, Long followedId);

    /**
     * Lista los seguidores de un usuario (quién le sigue).
     */
    Page<Follow> findByFollowedIdOrderByCreatedAtDesc(Long followedId, Pageable pageable);

    /**
     * Lista los seguidos de un usuario (a quién sigue).
     */
    Page<Follow> findByFollowerIdOrderByCreatedAtDesc(Long followerId, Pageable pageable);

    /**
     * Cuenta los seguidores de un usuario.
     */
    long countByFollowedId(Long followedId);

    /**
     * Cuenta a cuántos usuarios sigue.
     */
    long countByFollowerId(Long followerId);

    void deleteByFollowerIdAndFollowedId(Long followerId, Long followedId);

    List<Follow> findAllByFollowedId(Long followedId);
}
