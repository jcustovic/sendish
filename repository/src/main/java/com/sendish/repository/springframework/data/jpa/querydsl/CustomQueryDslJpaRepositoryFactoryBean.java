package com.sendish.repository.springframework.data.jpa.querydsl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactory;
import org.springframework.data.jpa.repository.support.JpaRepositoryFactoryBean;
import org.springframework.data.repository.core.RepositoryMetadata;
import org.springframework.data.repository.core.support.RepositoryFactorySupport;

import javax.persistence.EntityManager;
import java.io.Serializable;

public class CustomQueryDslJpaRepositoryFactoryBean<R extends JpaRepository<T, I>, T, I extends Serializable>
        extends JpaRepositoryFactoryBean<R, T, I> {

    protected RepositoryFactorySupport createRepositoryFactory(EntityManager entityManager) {
        return new CustomQueryDslJpaRepositoryFactory(entityManager);
    }

    private static class CustomQueryDslJpaRepositoryFactory<T, I extends Serializable> extends JpaRepositoryFactory {
        private EntityManager entityManager;

        public CustomQueryDslJpaRepositoryFactory(EntityManager entityManager) {
            super(entityManager);
            this.entityManager = entityManager;
        }

        protected Object getTargetRepository(RepositoryMetadata metadata) {
            return new CustomQueryDslJpaRepositoryImpl<>(getEntityInformation(metadata.getDomainType()), entityManager);
        }

        protected Class<?> getRepositoryBaseClass(RepositoryMetadata metadata) {
            return CustomQueryDslJpaRepository.class;
        }
    }

}
