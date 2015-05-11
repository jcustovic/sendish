package com.sendish.api.dto;

import java.io.Serializable;

public class ReportTypeDto implements Serializable {

    private String value;
    private String name;
    private Boolean requiresText;

    public ReportTypeDto() {
        super();
    }

    public ReportTypeDto(String value, String name, Boolean requiresText) {
        super();
        this.value = value;
        this.name = name;
        this.requiresText = requiresText;
    }

    // Getters & setters

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean isRequiresText() {
        return requiresText;
    }

    public void setRequiresText(Boolean requiresText) {
        this.requiresText = requiresText;
    }

}
