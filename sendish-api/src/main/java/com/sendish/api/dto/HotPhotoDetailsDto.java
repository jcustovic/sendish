package com.sendish.api.dto;

public class HotPhotoDetailsDto extends PhotoDetailsDto {

	private static final long serialVersionUID = 1L;
	
	private Boolean like;

    public Boolean getLike() {
        return like;
    }

    public void setLike(Boolean like) {
        this.like = like;
    }

}
