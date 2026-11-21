package io.github.codeladyship.hellofresh.service;

import io.github.codeladyship.hellofresh.dto.MealKitResponse;
import io.github.codeladyship.hellofresh.dto.PageResponse;
import io.github.codeladyship.hellofresh.entity.MealKit;
import io.github.codeladyship.hellofresh.exception.NotFoundException;
import io.github.codeladyship.hellofresh.repository.MealKitRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class MealKitService {

    private final MealKitRepository repository;

    public MealKitService(MealKitRepository repository) {
        this.repository = repository;
    }

    /** Lists meal kits, optionally only those with the given tag (case-insensitive). */
    public PageResponse<MealKitResponse> list(String tag, Pageable pageable) {
        Page<MealKit> page = StringUtils.hasText(tag)
                ? repository.findByTag(tag.trim(), pageable)
                : repository.findAll(pageable);
        return PageResponse.of(page, MealKitResponse::from);
    }

    public MealKitResponse getBySlug(String slug) {
        return repository.findBySlug(slug)
                .map(MealKitResponse::from)
                .orElseThrow(() -> new NotFoundException("Meal kit '" + slug + "' was not found"));
    }
}
