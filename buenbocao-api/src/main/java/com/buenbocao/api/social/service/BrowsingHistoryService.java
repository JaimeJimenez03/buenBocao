package com.buenbocao.api.social.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.buenbocao.api.common.exception.ResourceNotFoundException;
import com.buenbocao.api.security.CustomUserDetails;
import com.buenbocao.api.social.dto.response.BrowsingHistoryDTO;
import com.buenbocao.api.social.entity.BrowsingHistory;
import com.buenbocao.api.social.repository.BrowsingHistoryRepository;
import com.buenbocao.api.user.entity.User;
import com.buenbocao.api.user.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de Historial de Busqueda.
 * Permite añadir, quitar y listar recetas favoritas.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class BrowsingHistoryService {

    private final BrowsingHistoryRepository bHRepository;
    private final UserRepository userRepository;

    /**
     * Devuelve los 3 ultimos resultados de la busqueda.
     */

    @Transactional
    public List<BrowsingHistoryDTO> getBrowsingHistories(CustomUserDetails currentUser) {

        if (currentUser == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

        return bHRepository.findTop3ByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(h -> new BrowsingHistoryDTO(
                        h.getId(),
                        h.getContent()))
                .toList();
    }

    /**
     * Guarda una búsqueda en el historial.
     * Si ya existe el mismo término, lo borra primero para no duplicar
     * y lo vuelve a insertar como el más reciente.
     * Mantiene un máximo de 10 entradas por usuario.
     */
    @Transactional
    public void saveBrowsingHistory(CustomUserDetails currentUser, String query) {
        if (query == null || query.isBlank())
            return;

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

        // Borrar si ya existía el mismo término (para que suba al top)
        bHRepository.deleteByUserIdAndContent(user.getId(), query.trim());

        // Guardar nueva entrada
        BrowsingHistory entry = BrowsingHistory.builder()
                .user(user)
                .content(query.trim())
                .build();
        bHRepository.save(entry);

        // Mantener solo las últimas 10 entradas
        List<BrowsingHistory> all = bHRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        if (all.size() > 10) {
            List<BrowsingHistory> toDelete = all.subList(10, all.size());
            bHRepository.deleteAll(toDelete);
        }

        log.debug("Búsqueda guardada para {}: {}", user.getUsername(), query);
    }

    /**
     * Elimina una entrada del historial de búsqueda del usuario.
     */
    @Transactional
    public void deleteBrowsingHistory(CustomUserDetails currentUser, Long historyId) {

        if (currentUser == null) {
            throw new IllegalArgumentException("Usuario no autenticado");
        }

        User user = userRepository.findById(currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", currentUser.getId()));

        BrowsingHistory history = bHRepository.findById(historyId)
                .orElseThrow(() -> new ResourceNotFoundException("Historial", "id", historyId));

        // 🔒 seguridad: asegurar que el historial pertenece al usuario
        if (!history.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este historial");
        }

        bHRepository.delete(history);

        log.debug("Historial eliminado para {}: {}", user.getUsername(), historyId);
    }

}
