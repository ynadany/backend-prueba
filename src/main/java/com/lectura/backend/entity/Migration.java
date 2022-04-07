package com.lectura.backend.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Builder
@Table(schema = "libranda")
public class Migration extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDateTime dateTime;
    private String lastUrl;
    private SynchronizationEnum type = SynchronizationEnum.FULL;
    private boolean finished = false;
    private Integer count;

    public static Migration findLast(SynchronizationEnum type) {
        return find("finished = false AND type = ?1", Sort.descending("dateTime"), type).firstResult();
    }
}
