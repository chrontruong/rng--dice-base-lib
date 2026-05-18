package com.io.begstd.slot.model.domain;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;

// @Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) /* need for ORM */
public class Money implements Comparable<Money>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -4173092700474431573L;

    public static final Money ZERO = Money.of(new BigDecimal("0.0000"));

    private BigDecimal value;

    // IMPORTANT: do not remove this, jackson use this to deserialize money
    private Money(@NonNull String value) {
        this(new BigDecimal(value));
    }

    public Money(BigDecimal bigDecimal) {
        // Asserts.notNull(bigDecimal, "Money value must not be null");
        this.value = bigDecimal.setScale(4, RoundingMode.HALF_EVEN);
    }
    
    public Money(BigDecimal bigDecimal, int decimal) {
        // Asserts.notNull(bigDecimal, "Money value must not be null");
        this.value = bigDecimal.setScale(decimal, RoundingMode.HALF_EVEN);
    }

    public static Money of(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return new Money(value);
    }
    
    public static Money of(BigDecimal value, int decimal) {
        if (value == null) {
            return null;
        }
        return new Money(value, decimal);
    }

    public static Money of(String value) {
        if (value == null) {
            return null;
        }
        return new Money(new BigDecimal(value));
    }

    public static Money of(long value) {
        return Money.of(String.valueOf(value));
    }

    public static Money of(int value) {
        return Money.of(value + "");
    }

    public static Money of(double value) {
        return new Money(new BigDecimal(value));
    }

    public static Money of(double value, int decimal) {
        return new Money(new BigDecimal(value), decimal);
    }
    
    public Money multiply(int value) {
        return new Money(this.value.multiply(new BigDecimal(value + "")));
    }

    public Money multiply(long value) {
        return new Money(this.value.multiply(new BigDecimal(value + "")));
    }

    public Money multiply(BigDecimal value) {
        return new Money(this.value.multiply(value));
    }

    public Money multiply(double value) {
        return new Money(this.value.multiply(new BigDecimal(value)));
    }

    public Money multiply(Money value) {
        return new Money(this.value.multiply(value.value()));
    }

    public Money divide(Money value) {
        return new Money(this.value.divide(value.value(), RoundingMode.HALF_EVEN));
    }

    public Money divide(long value) {
        return new Money(this.value.divide(new BigDecimal(value + ""), RoundingMode.HALF_EVEN));
    }

    public Money add(Money price) {
        return new Money(this.value.add(price.value));
    }

    public Money add(BigDecimal price) {
        return new Money(this.value.add(price));
    }

    public Money subtract(Money v) {
        return new Money(this.value.subtract(v.value));
    }

    public Money negate() {
        return new Money(this.value.negate());
    }

    public boolean lt(Money value) {
        return isLessThan(value);
    }

    public boolean isLessThan(Money value) {
        return compareTo(value) < 0;
    }

    public boolean lte(Money value) {
        return isLessThanOrEqual(value);
    }

    public boolean isLessThanOrEqual(Money value) {
        return compareTo(value) <= 0;
    }

    public boolean gt(Money value) {
        return isGreaterThan(value);
    }

    public boolean isGreaterThan(Money value) {
        return compareTo(value) > 0;
    }

    public boolean gte(Money value) {
        return isGreaterThanOrEqual(value);
    }

    public boolean isGreaterThanOrEqual(Money value) {
        return compareTo(value) >= 0;
    }

    public boolean eq(Money value) {
        return isEqual(value);
    }

    public boolean isEqual(Money value) {
        return compareTo(value) == 0;
    }

    @JsonValue
    public String asText() {
        return this.value.toPlainString();
    }

    public BigDecimal value() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Money)) {
            return false;
        }
        Money price = (Money) o;
        return this.value.equals(price.value);
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public String toString() {
        return this.value.toString();
    }

    @Override
    public int compareTo(Money o) {
        return this.value.compareTo(o.value);
    }
}
