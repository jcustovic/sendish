package com.sendish.repository.springframework.data.jpa.querydsl;

import com.mysema.query.jpa.JPQLQuery;
import com.mysema.query.types.EntityPath;
import com.mysema.query.types.FactoryExpression;
import com.mysema.query.types.Predicate;
import com.mysema.query.types.path.PathBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.QueryDslJpaRepository;
import org.springframework.data.jpa.repository.support.Querydsl;
import org.springframework.data.querydsl.EntityPathResolver;
import org.springframework.data.querydsl.SimpleEntityPathResolver;

import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class CustomQueryDslJpaRepositoryImpl<T, ID extends Serializable> extends QueryDslJpaRepository<T, ID>
        implements CustomQueryDslJpaRepository<T, ID> {

    //All instance variables are available in super, but they are private
    private static final EntityPathResolver DEFAULT_ENTITY_PATH_RESOLVER = SimpleEntityPathResolver.INSTANCE;

    private final EntityPath<T> path;
    private final PathBuilder<T> builder;
    private final Querydsl querydsl;

    public CustomQueryDslJpaRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager) {
        this(entityInformation, entityManager, DEFAULT_ENTITY_PATH_RESOLVER);
    }

    public CustomQueryDslJpaRepositoryImpl(JpaEntityInformation<T, ID> entityInformation, EntityManager entityManager,
                                           EntityPathResolver resolver) {
        super(entityInformation, entityManager);
        this.path = resolver.createPath(entityInformation.getJavaType());
        this.builder = new PathBuilder<T>(path.getType(), path.getMetadata());
        this.querydsl = new Querydsl(entityManager, builder);
    }

    @Override
    public <K> Page<K> findAll(FactoryExpression<K> factoryExpression, Predicate predicate, Pageable pageable) {
        JPQLQuery countQuery = createQuery(predicate);
        JPQLQuery query = querydsl.applyPagination(pageable, createQuery(predicate));

        Long total = countQuery.count();
        List<K> content = total > pageable.getOffset() ? query.list(factoryExpression) : Collections.emptyList();

        return new PageImpl(content, pageable, total);
    }

}