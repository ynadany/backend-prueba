package com.lectura.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = "libranda")
public class Price {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String countryCode;
    private String currencyCode;
    private Double priceAmount;
    private Integer date;
    private Byte role;
    private String type;

    private boolean migrated;

    @ManyToOne(cascade = CascadeType.REFRESH)
    @JoinColumn(name = "idPublication", nullable = false)
    private Publication publication;

    public Integer getIntegerPriceAmount() {
        return ((Long) Math.round(priceAmount * 100)).intValue();
    }
}
