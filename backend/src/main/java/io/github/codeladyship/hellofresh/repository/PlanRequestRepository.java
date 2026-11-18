package io.github.codeladyship.hellofresh.repository;

import io.github.codeladyship.hellofresh.entity.PlanRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRequestRepository extends JpaRepository<PlanRequest, Long> {
}
