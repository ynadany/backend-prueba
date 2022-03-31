package com.lectura.backend.repository;

import com.lectura.backend.entity.Publisher;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class PublisherRepository implements PanacheRepositoryBase<Publisher, String> {
    public List<Publisher> findToSynchronize() {
        return list("tagId = null");
    }
}
