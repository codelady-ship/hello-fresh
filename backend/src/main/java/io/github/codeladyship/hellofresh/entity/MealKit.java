package io.github.codeladyship.hellofresh.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.Set;
import java.util.TreeSet;

/** A meal kit / recipe category shown in the "Over 30+ fresh recipes every week" carousel. */
@Entity
@Table(name = "meal_kit")
public class MealKit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String slug;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 160)
    private String subtitle;

    @Column(nullable = false, length = 1000)
    private String description;

    @Column(name = "image_url", nullable = false, length = 500)
    private String imageUrl;

    @Column(name = "cooking_time_minutes", nullable = false)
    private int cookingTimeMinutes;

    @Column(nullable = false)
    private int calories;

    @Column(name = "price_per_serving", nullable = false, precision = 6, scale = 2)
    private BigDecimal pricePerServing;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @ElementCollection
    @CollectionTable(name = "meal_kit_tag", joinColumns = @JoinColumn(name = "meal_kit_id"))
    @Column(name = "tag", nullable = false, length = 40)
    @BatchSize(size = 50)
    private Set<String> tags = new TreeSet<>();

    protected MealKit() {
        // for JPA
    }

    public MealKit(String slug, String title, String subtitle, String description, String imageUrl,
                   int cookingTimeMinutes, int calories, BigDecimal pricePerServing, int sortOrder, Set<String> tags) {
        this.slug = slug;
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.imageUrl = imageUrl;
        this.cookingTimeMinutes = cookingTimeMinutes;
        this.calories = calories;
        this.pricePerServing = pricePerServing;
        this.sortOrder = sortOrder;
        this.tags = new TreeSet<>(tags);
    }

    public Long getId() { return id; }
    public String getSlug() { return slug; }
    public String getTitle() { return title; }
    public String getSubtitle() { return subtitle; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }
    public int getCookingTimeMinutes() { return cookingTimeMinutes; }
    public int getCalories() { return calories; }
    public BigDecimal getPricePerServing() { return pricePerServing; }
    public int getSortOrder() { return sortOrder; }
    public Set<String> getTags() { return tags; }
}
