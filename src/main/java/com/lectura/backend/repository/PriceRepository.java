package com.lectura.backend.repository;

import com.lectura.backend.entity.Price;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class PriceRepository implements PanacheRepositoryBase<Price, Long> {
}
