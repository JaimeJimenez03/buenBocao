package com.buenbocao.api.social.service;

import com.buenbocao.api.common.constants.AppConstants;
import com.buenbocao.api.common.exception.BusinessException;
import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.common.response.PagedResponse;
import com.buenbocao.api.notification.service.NotificationService;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.repository.FollowRepository;
import com.buenbocao.api.social.entity.Follow;
import com.buenbocao.api.user.dto.response.UserSummaryResponse;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.mapper.UserMapper;
import com.buenbocao.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Servicio de seguidores/seguidos.
 * Permite seguir, dejar de seguir y listar relaciones de seguimiento.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final NotificationService notificationService;

    /**
     * Seguir o dejar de seguir a un usuario (toggle).
     * Devuelve true si se siguió, false si se dejó de seguir.
     */
    @Transactional
    public boolean toggleFollow(CustomUserDetails currentUser, Long targetUserId) {
        if (currentUser.getId().equals(targetUserId)) {
            throw new BusinessException("No puedes seguirte a ti mismo");
        }

        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", targetUserId));

        boolean exists = followRepository.existsByFollowerIdAndFollowedId(
                currentUser.getId(), targetUserId);

        log.info("toggleFollow → follower={} target={} exists={}",
                currentUser.getId(), targetUserId, exists);

        if (exists) {
            followRepository.deleteByFollowerIdAndFollowedId(
                    currentUser.getId(), targetUserId);
            followRepository.flush();
            log.info("Dejó de seguir: {} → {}", currentUser.getId(), targetUserId);
            return false;
        } else {
            User follower = userRepository.findById(currentUser.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

            Follow follow = Follow.builder()
                    .follower(follower)
                    .followed(targetUser)
                    .build();

            followRepository.save(follow);
            followRepository.flush();
            log.info("Empezó a seguir: {} → {}", currentUser.getId(), targetUserId);
            notificationService.notifyNewFollower(
                    follower.getId(),
                    follower.getUsername(),
                    follower.getProfileImage(),
                    targetUser.getId());
            return true;
        }
    }

    /**
     * Lista los seguidores de un usuario.
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryResponse> getFollowers(Long userId, int page, int size) {
        // Verificar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario", "id", userId);
        }

        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        Page<Follow> followsPage = followRepository.findByFollowedIdOrderByCreatedAtDesc(userId, pageable);

        List<UserSummaryResponse> content = followsPage.getContent().stream()
                .map(follow -> userMapper.toSummaryResponse(follow.getFollower()))
                .toList();

        return PagedResponse.of(followsPage, content);
    }

    /**
     * Lista los usuarios que sigue un usuario.
     */
    @Transactional(readOnly = true)
    public PagedResponse<UserSummaryResponse> getFollowing(Long userId, int page, int size) {
        // Verificar que el usuario existe
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario", "id", userId);
        }

        size = Math.min(size, AppConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, size);

        Page<Follow> followsPage = followRepository.findByFollowerIdOrderByCreatedAtDesc(userId, pageable);

        List<UserSummaryResponse> content = followsPage.getContent().stream()
                .map(follow -> userMapper.toSummaryResponse(follow.getFollowed()))
                .toList();

        return PagedResponse.of(followsPage, content);
    }

    /**
     * Comprueba si un usuario sigue a otro.
     */
    @Transactional(readOnly = true)
    public boolean isFollowing(Long followerId, Long followedId) {
        return followRepository.existsByFollowerIdAndFollowedId(followerId, followedId);
    }

    /**
     * Cuenta los seguidores de un usuario.
     */
    @Transactional(readOnly = true)
    public long countFollowers(Long userId) {
        return followRepository.countByFollowedId(userId);
    }

    /**
     * Cuenta a cuántos usuarios sigue.
     */
    @Transactional(readOnly = true)
    public long countFollowing(Long userId) {
        return followRepository.countByFollowerId(userId);
    }
}
