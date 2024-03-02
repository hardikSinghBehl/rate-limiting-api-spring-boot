package com.behl.overseer.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.behl.overseer.entity.UserPlanMapping;

@Repository
public interface UserPlanMappingRepository extends JpaRepository<UserPlanMapping, UUID> {

	Boolean existsByUserIdAndPlanIdAndIsActive(final UUID userId, final UUID planId, final Boolean isActive);

	@Modifying
	@Query(nativeQuery = true, value = "UPDATE user_plan_mappings SET is_active = false WHERE user_id = ?1 and is_active = true")
	void deactivateCurrentPlan(final UUID userId);

	UserPlanMapping findByUserIdAndIsActive(final UUID userId, final Boolean isActive);

}