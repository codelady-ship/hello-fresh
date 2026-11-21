package io.github.codeladyship.hellofresh.repository;

import io.github.codeladyship.hellofresh.entity.MealKit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MealKitRepository extends JpaRepository<MealKit, Long> {

    Optional<MealKit> findBySlug(String slug);

    @Query(value = "select m from MealKit m join m.tags t where lower(t) = lower(:tag)",
           countQuery = "select count(m) from MealKit m join m.tags t where lower(t) = lower(:tag)")
    Page<MealKit> findByTag(@Param("tag") String tag, Pageable pageable);
}
