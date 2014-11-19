package com.sendish.api.dto;

import java.io.Serializable;

public class BaseEntityDto implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
