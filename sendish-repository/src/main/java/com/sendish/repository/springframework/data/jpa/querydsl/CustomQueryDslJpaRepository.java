package com.sendish.repository.springframework.data.jpa.querydsl;

import com.mysema.query.types.FactoryExpression;
import com.mysema.query.types.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.io.Serializable;

@NoRepositoryBean
public interface CustomQueryDslJpaRepository<T, ID extends Serializable> extends JpaRepository<T, ID>, QueryDslPredicateExecutor<T>, JpaSpecificationExecutor<T> {

    /**
     * Returns a {@link org.springframework.data.domain.Page} of entities matching the given {@link com.mysema.query.types.Predicate}.
     * This also uses provided projections ( can be JavaBean or constructor or anything supported by QueryDSL
     *
     * @param factoryExpression this constructor expression will be used for transforming query results
     * @param predicate
     * @param pageable
     * @return
     */
    <K> Page<K> findAll(FactoryExpression<K> factoryExpression, Predicate predicate, Pageable pageable);

}
