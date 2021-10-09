package com.behl.glumon.bootstrap;

import java.util.List;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Configuration;

import com.behl.glumon.entity.Plan;
import com.behl.glumon.repository.PlanRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Configuration
@AllArgsConstructor
@Slf4j
public class PlanDataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final PlanRepository planRepository;

    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {
        final var plans = List.of(List.of("FREE", "20"), List.of("BUSINESS", "40"), List.of("PROFESSIONAL", "100"));
        plans.forEach(planDetials -> {
            final var plan = new Plan();
            plan.setName(planDetials.get(0));
            plan.setLimitPerHour(Integer.parseInt(planDetials.get(1)));
            final var savedPlan = planRepository.save(plan);

            log.info("{} plan created with {} limit-per-hour created successfully", savedPlan.getName(),
                    savedPlan.getLimitPerHour());
        });
        log.info("{} Plan(s) created successfully.", plans.size());
    }

}
