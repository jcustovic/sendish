package com.sendish.repository.model.jpa;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idSequence")
    @Column(name = "id")
    private Long              id;

    public Long getId() {
        return id;
    }

    // public void setId(final Long p_id) {
    // this.id = p_id;
    // }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((getId() == null) ? 0 : getId().hashCode());

        return result;
    }

    @Override
    public boolean equals(final Object p_obj) {
        if (this == p_obj) {
            return true;
        }
        if (p_obj == null) {
            return false;
        }
        if (getClass() != p_obj.getClass()) {
            return false;
        }
        final BaseEntity other = (BaseEntity) p_obj;
        if (getId() == null) {
            return false;
        } else if (!getId().equals(other.getId())) {
            return false;
        }

        return true;
    }

}