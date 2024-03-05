package com.behl.overseer.entity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Entity
@ToString
@Table(name = "user_plan_mappings")
public class UserPlanMapping {

	@Id
	@Setter(AccessLevel.NONE)
	@Column(name = "id", nullable = false, unique = true)
	private UUID id;

	@Column(name = "user_id", nullable = true)
	private UUID userId;

	@Setter(AccessLevel.NONE)
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "user_id", nullable = true, insertable = false, updatable = false)
	private User user;

	@Column(name = "plan_id", nullable = true)
	private UUID planId;

	@Setter(AccessLevel.NONE)
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "plan_id", nullable = true, insertable = false, updatable = false)
	private Plan plan;

	@Column(name = "is_active", nullable = false)
	private Boolean isActive;

	@Setter(AccessLevel.NONE)
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Setter(AccessLevel.NONE)
	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@PrePersist
	void onCreate() {
		this.id = UUID.randomUUID();
		this.isActive = Boolean.TRUE;
		this.createdAt = LocalDateTime.now(ZoneOffset.UTC);
		this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = LocalDateTime.now(ZoneOffset.UTC);
	}

}