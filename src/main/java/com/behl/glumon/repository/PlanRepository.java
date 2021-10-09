package com.behl.glumon.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.behl.glumon.entity.Plan;

@Repository
public interface PlanRepository extends JpaRepository<Plan, UUID> {

}