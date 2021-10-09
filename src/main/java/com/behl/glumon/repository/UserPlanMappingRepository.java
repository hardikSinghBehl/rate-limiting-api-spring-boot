package com.behl.glumon.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.behl.glumon.entity.Plan;
import com.behl.glumon.entity.User;
import com.behl.glumon.entity.UserPlanMapping;

@Repository
public interface UserPlanMappingRepository extends JpaRepository<UserPlanMapping, Integer> {

    Boolean existsByUserAndPlan(final User user, final Plan plan);

    @Modifying
    @Query(nativeQuery = true, value = "UPDATE user_plan_mappings SET is_active = false WHERE user_id = ?1")
    void invalidatePreviousPlans(final UUID user);

    Optional<UserPlanMapping> findByUserIdAndIsActive(final UUID userId, final Boolean isActives);

}
