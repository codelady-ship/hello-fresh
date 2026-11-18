package io.github.codeladyship.hellofresh.service;

import io.github.codeladyship.hellofresh.entity.MealKit;
import io.github.codeladyship.hellofresh.exception.NotFoundException;
import io.github.codeladyship.hellofresh.repository.MealKitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealKitServiceTest {

    @Mock
    private MealKitRepository repository;

    @InjectMocks
    private MealKitService service;

    private static MealKit kit(String slug, String... tags) {
        return new MealKit(slug, slug, "sub", "desc", "images/x.jpg", 20, 500, new BigDecimal("7.49"), 1, Set.of(tags));
    }

    @Test
    void listWithoutTagUsesFindAll() {
        PageRequest pageable = PageRequest.of(0, 12);
        when(repository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(kit("veggie", "vegetarian")), pageable, 1));

        var page = service.list("  ", pageable);

        assertThat(page.content()).extracting("slug").containsExactly("veggie");
        verify(repository, never()).findByTag(anyString(), any());
    }

    @Test
    void listWithTagTrimsAndFilters() {
        PageRequest pageable = PageRequest.of(0, 12);
        when(repository.findByTag("family", pageable))
                .thenReturn(new PageImpl<>(List.of(kit("family-friendly", "quick", "family")), pageable, 1));

        var page = service.list(" family ", pageable);

        assertThat(page.content().getFirst().tags()).containsExactly("family", "quick");
    }

    @Test
    void getBySlugThrowsWhenMissing() {
        when(repository.findBySlug("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBySlug("missing"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("missing");
    }
}
