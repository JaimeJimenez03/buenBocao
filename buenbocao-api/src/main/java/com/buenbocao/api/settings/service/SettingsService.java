package com.buenbocao.api.settings.service;

import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.settings.dto.request.UpdateSettingsRequest;
import com.buenbocao.api.settings.dto.response.UserSettingsResponse;
import com.buenbocao.api.settings.entity.UserSettings;
import com.buenbocao.api.settings.repository.UserSettingsRepository;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final UserSettingsRepository settingsRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public UserSettingsResponse getSettings(Long userId) {
        return toResponse(getOrCreate(userId));
    }

    @Transactional
    public UserSettingsResponse updateSettings(Long userId, UpdateSettingsRequest request) {
        UserSettings settings = getOrCreate(userId);

        // Apariencia
        if (request.getTheme() != null)
            settings.setTheme(request.getTheme());
        if (request.getLanguage() != null)
            settings.setLanguage(request.getLanguage());

        // Notificaciones en app
        if (request.getNotifyNewFollower() != null)
            settings.setNotifyNewFollower(request.getNotifyNewFollower());
        if (request.getNotifyNewReview() != null)
            settings.setNotifyNewReview(request.getNotifyNewReview());
        if (request.getNotifyNewFavorite() != null)
            settings.setNotifyNewFavorite(request.getNotifyNewFavorite());
        if (request.getNotifyEvents() != null)
            settings.setNotifyEvents(request.getNotifyEvents());
        if (request.getNotifyEmail() != null)
            settings.setNotifyEmail(request.getNotifyEmail());

        // Push — pushNewFollower es siempre true, ignorar lo que mande el cliente
        settings.setPushNewFollower(true);
        if (request.getPushNewReview() != null)
            settings.setPushNewReview(request.getPushNewReview());
        if (request.getPushNewFavorite() != null)
            settings.setPushNewFavorite(request.getPushNewFavorite());
        if (request.getPushEvents() != null)
            settings.setPushEvents(request.getPushEvents());
        if (request.getPushSystem() != null)
            settings.setPushSystem(request.getPushSystem());

        // Expo push token (se guarda en User, no en UserSettings)
        if (request.getExpoPushToken() != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
            user.setExpoPushToken(request.getExpoPushToken());
            userRepository.save(user);
        }

        // Cocina
        if (request.getMeasurementSystem() != null)
            settings.setMeasurementSystem(request.getMeasurementSystem());

        // Privacidad
        if (request.getPublicProfile() != null)
            settings.setPublicProfile(request.getPublicProfile());
        if (request.getShowActivity() != null)
            settings.setShowActivity(request.getShowActivity());

        return toResponse(settingsRepository.save(settings));
    }

    @Transactional
    public UserSettingsResponse resetSettings(Long userId) {
        UserSettings settings = getOrCreate(userId);
        settings.setTheme("system");
        settings.setLanguage("es");
        settings.setNotifyNewFollower(true);
        settings.setNotifyNewReview(true);
        settings.setNotifyNewFavorite(true);
        settings.setNotifyEvents(true);
        settings.setNotifyEmail(true);
        settings.setPushNewFollower(true);
        settings.setPushNewReview(true);
        settings.setPushNewFavorite(true);
        settings.setPushEvents(true);
        settings.setPushSystem(false);
        settings.setMeasurementSystem("metric");
        settings.setPublicProfile(true);
        settings.setShowActivity(true);
        return toResponse(settingsRepository.save(settings));
    }

    private UserSettings getOrCreate(Long userId) {
        return settingsRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));
                    return settingsRepository.save(UserSettings.builder().user(user).build());
                });
    }

    private UserSettingsResponse toResponse(UserSettings s) {
        return UserSettingsResponse.builder()
                .theme(s.getTheme())
                .language(s.getLanguage())
                .notifyNewFollower(s.isNotifyNewFollower())
                .notifyNewReview(s.isNotifyNewReview())
                .notifyNewFavorite(s.isNotifyNewFavorite())
                .notifyEvents(s.isNotifyEvents())
                .notifyEmail(s.isNotifyEmail())
                .pushNewFollower(true)
                .pushNewReview(s.isPushNewReview())
                .pushNewFavorite(s.isPushNewFavorite())
                .pushEvents(s.isPushEvents())
                .pushSystem(s.isPushSystem())
                .measurementSystem(s.getMeasurementSystem())
                .publicProfile(s.isPublicProfile())
                .showActivity(s.isShowActivity())
                .build();
    }
}