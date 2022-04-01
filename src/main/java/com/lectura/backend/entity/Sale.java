package com.lectura.backend.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Optional;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = "libranda")
public class Sale extends PanacheEntityBase {
    @Id
    private String id;
    private String customer;
    private LocalDateTime dateTime;
    private String sku;
    private String format;
    private Short quantity;
    private String currency;
    private Double price;

    private String token;
    private boolean downloaded;

    public static Optional<Sale> findByToken(String token) {
        return find("downloaded = false AND token = :token",
                Parameters.with("token", token)).singleResultOptional();
    }
}
