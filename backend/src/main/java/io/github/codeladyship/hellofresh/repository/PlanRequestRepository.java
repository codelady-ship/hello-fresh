package io.github.codeladyship.hellofresh.repository;

import io.github.codeladyship.hellofresh.entity.PlanRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface PlanRequestRepository extends JpaRepository<PlanRequest, Long> {

    /** Bulk delete used by the retention job. */
    @Modifying(clearAutomatically = true)
    @Query("delete from PlanRequest r where r.createdAt < :cutoff")
    int deleteByCreatedAtBefore(@Param("cutoff") Instant cutoff);
}
