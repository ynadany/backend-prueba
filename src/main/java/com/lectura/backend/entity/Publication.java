package com.lectura.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(schema = "libranda")
public class Publication {
    public final static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Id
    private String id;
    private String isbn;
    private String productFormDetail;
    private String language;
    private String title;
    private String author;
    @Column(columnDefinition = "TEXT")
    private String textContent;
    private Integer publishingDate;
    private String publishingStatus;
    @Column(length = 1000)
    private String salesRights;
    private String subjectBicCode;

    @Column(length = 1000)
    private String marketCountries;
    private String marketPublishingStatus;
    private Integer marketDate;
    private String technicalProtection;
    private Double exchangeRate;

    @OneToMany(mappedBy = "publication", cascade = CascadeType.ALL)
    private List<Price> prices;

    @OneToOne(mappedBy = "publication", cascade = CascadeType.ALL)
    private Media media;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "idPublisher", nullable = false)
    private Publisher publisher;

    @NotNull
    @Column(columnDefinition = "boolean default false")
    private boolean updated = false;
    private Long productId;

    public Price getPrice() throws Exception {
        if (prices.stream().anyMatch(p -> p.getCurrencyCode().equals("BOB"))) {
            var list = prices.stream().filter(p -> p.getCurrencyCode().equals("BOB")).collect(Collectors.toList());
            if (list.size() == 1) {
                return list.get(0);
            } else {
                var priceFrom = list.stream().filter(p -> p.getRole().equals(Byte.valueOf("14")) && Integer.parseInt(LocalDateTime.now().format(formatter)) >= p.getDate())
                        .findFirst();
                if (priceFrom.isPresent()) {
                    return priceFrom.get();
                } else {
                    return list.stream().filter(p ->
                            (p.getRole().equals(Byte.valueOf("15")) && Integer.parseInt(LocalDateTime.now().format(formatter)) <= p.getDate()) ||
                                    p.getRole().equals(Byte.valueOf("15"))).findFirst().get();
                }
            }
        } else if (prices.stream().anyMatch(p -> p.getCurrencyCode().equals("USD"))) {
            var list = prices.stream().filter(p -> p.getCurrencyCode().equals("USD")).collect(Collectors.toList());
            if (list.size() == 1) {
                return list.get(0);
            } else {
                var priceFrom = list.stream().filter(p -> Integer.parseInt(LocalDateTime.now().format(formatter)) == p.getDate())
                        .findFirst();
                if (priceFrom.isPresent()) {
                    return priceFrom.get();
                } else {
                    return list.stream().filter(p -> p.getType().equals("15")).findFirst().get();
                }
            }
        }
        throw new Exception("Does not found a publication's price");
    }
}
