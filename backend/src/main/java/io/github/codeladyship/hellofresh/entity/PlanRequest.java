package io.github.codeladyship.hellofresh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;

/** A plan request sent from the "Flexible Plans" form on the landing page. */
@Entity
@Table(name = "plan_request")
public class PlanRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 254)
    private String email;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "meal_kit_id")
    private MealKit mealKit;

    /** Price snapshot at the time of the request, so later price changes do not alter it. */
    @Column(name = "weekly_total", nullable = false, precision = 8, scale = 2)
    private BigDecimal weeklyTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PlanRequestStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected PlanRequest() {
        // for JPA
    }

    public PlanRequest(String name, String email, Plan plan, MealKit mealKit, Instant createdAt) {
        this.name = name;
        this.email = email;
        this.plan = plan;
        this.mealKit = mealKit;
        this.weeklyTotal = plan.weeklyTotal();
        this.status = PlanRequestStatus.RECEIVED;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Plan getPlan() { return plan; }
    public MealKit getMealKit() { return mealKit; }
    public BigDecimal getWeeklyTotal() { return weeklyTotal; }
    public PlanRequestStatus getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}
