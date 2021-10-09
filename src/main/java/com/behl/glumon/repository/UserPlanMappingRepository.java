package com.behl.glumon.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.behl.glumon.entity.UserPlanMapping;

@Repository
public interface UserPlanMappingRepository extends JpaRepository<UserPlanMapping, Integer> {

}
