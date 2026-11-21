package io.github.codeladyship.hellofresh.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;

/** A "flexible plan": number of people x meals per week, with its price per serving. */
@Entity
@Table(name = "plan")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int people;

    @Column(name = "meals_per_week", nullable = false)
    private int mealsPerWeek;

    @Column(name = "price_per_serving", nullable = false, precision = 6, scale = 2)
    private BigDecimal pricePerServing;

    @Column(name = "shipping_fee", nullable = false, precision = 6, scale = 2)
    private BigDecimal shippingFee;

    protected Plan() {
        // for JPA
    }

    public Plan(int people, int mealsPerWeek, BigDecimal pricePerServing, BigDecimal shippingFee) {
        this.people = people;
        this.mealsPerWeek = mealsPerWeek;
        this.pricePerServing = pricePerServing;
        this.shippingFee = shippingFee;
    }

    public int servingsPerWeek() {
        return people * mealsPerWeek;
    }

    /** Weekly price: servings x price per serving + shipping, rounded to cents. */
    public BigDecimal weeklyTotal() {
        return pricePerServing.multiply(BigDecimal.valueOf(servingsPerWeek()))
                .add(shippingFee)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public int getPeople() { return people; }
    public int getMealsPerWeek() { return mealsPerWeek; }
    public BigDecimal getPricePerServing() { return pricePerServing; }
    public BigDecimal getShippingFee() { return shippingFee; }
}
