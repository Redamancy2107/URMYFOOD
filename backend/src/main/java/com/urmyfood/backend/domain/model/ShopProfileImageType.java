package com.urmyfood.backend.domain.model;

public enum ShopProfileImageType {
    LOGO("logo"),
    COVER("cover");

    private final String pathPrefix;

    ShopProfileImageType(String pathPrefix) {
        this.pathPrefix = pathPrefix;
    }

    public String getPathPrefix() {
        return pathPrefix;
    }
}
