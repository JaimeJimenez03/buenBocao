package com.buenbocao.api.social.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Media adjunta a una review (imágenes o vídeos).
 * Una review puede tener varias.
 */
@Entity
@Table(name = "review_media")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    /** Ruta relativa en el storage, ej: "reviews/uuid.jpg" */
    @Column(name = "media_url", nullable = false)
    private String mediaUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", nullable = false, length = 10)
    private MediaType mediaType;

    /** Orden de aparición dentro de la review. */
    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    public enum MediaType {
        IMAGE,
        VIDEO
    }
}
