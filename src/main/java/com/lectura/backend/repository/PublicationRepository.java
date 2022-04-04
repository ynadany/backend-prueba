package com.lectura.backend.repository;

import com.lectura.backend.entity.Publication;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import io.quarkus.panache.common.Page;

import javax.enterprise.context.ApplicationScoped;
import java.util.List;

@ApplicationScoped
public class PublicationRepository implements PanacheRepositoryBase<Publication, String> {
    public List<Publication> findToSynchronize() {
        return list("updated = ?1 OR productId = null", false);
    }

    public List<Publication> findToSynchronize(int page, int size) {
        return find("updated = ?1 OR productId = null", false)
                .page(Page.of(page, size)).list();
    }

    public Publication findByIsbn(String isbn) {
        return find("isbn", isbn).singleResult();
    }

    public Publication findByProductId(Long productId) {
        return find("productId", productId).singleResult();
    }
}
