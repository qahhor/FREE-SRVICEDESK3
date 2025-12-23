package io.greenwhite.servicedesk.ticket.model;

import io.greenwhite.servicedesk.common.model.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Knowledge Base Article entity
 */
@Entity
@Table(name = "kb_articles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KBArticle extends BaseEntity {

    @Column(nullable = false, length = 500)
    private String title;

    @Column(nullable = false, length = 500, unique = true)
    private String slug;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(length = 1000)
    private String excerpt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private KBCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private User author;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "helpful_count", nullable = false)
    @Builder.Default
    private Integer helpfulCount = 0;

    @Column(name = "not_helpful_count", nullable = false)
    @Builder.Default
    private Integer notHelpfulCount = 0;

    @Column(length = 500)
    private String tags;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    public enum ArticleStatus {
        DRAFT,
        PUBLISHED,
        ARCHIVED
    }
}
