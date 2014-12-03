package com.sendish.api.web.controller.model;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class ValidationError implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private Map<String, List<String>> errors;

	public ValidationError(Map<String, List<String>> errors) {
		super();
		this.errors = errors;
	}


	public Map<String, List<String>> getErrors() {
		return errors;
	}

}
