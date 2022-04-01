package com.lectura.backend.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Parameters;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Cacheable
@Table(schema = "libranda")
public class Category extends PanacheEntityBase {
    @Id
    private String code;
    private String description;
    private Integer categoryId;

    public static List<Category> findToSynchronize(List<String> bicCodes) {
        return list("categoryId = null AND code IN :codes", Parameters.with("codes", bicCodes));
    }
}
