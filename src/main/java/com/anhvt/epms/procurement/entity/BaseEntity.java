package com.anhvt.epms.procurement.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Base entity class containing common audit fields for all entities
 * Implements JPA Auditing for automatic tracking of creation and modification
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    UUID id;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    LocalDateTime updatedAt;
    
    @CreatedBy
    @Column(name = "created_by", length = 100, updatable = false)
    String createdBy;
    
    @LastModifiedBy
    @Column(name = "updated_by", length = 100)
    String updatedBy;
}
