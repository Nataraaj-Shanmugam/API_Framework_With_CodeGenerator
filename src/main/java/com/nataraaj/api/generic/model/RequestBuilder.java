package com.nataraaj.api.generic.model;

import com.nataraaj.api.generic.enums.AUTHORIZATION_TYPES;
import io.restassured.config.HttpClientConfig;
import io.restassured.filter.Filter;
import io.restassured.http.ContentType;
import io.restassured.http.Method;
import io.restassured.specification.ProxySpecification;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@Builder
public class RequestBuilder {

    @NonNull
    public Method methodType;
    public boolean relaxRequest;
    @NonNull
    public String baseURI;
    public String url;
    public Map<String, Object> headers;
    public Map<String, Object> queryParameter;
    public Map<String, Object> pathParameter;
    public Map<String, Object> formData;
    public Object body;
    public Authorization authorization;
    public int assertStatusCode;
    public boolean allowRedirects;
    public HttpClientConfig httpsConfig;
    public Map<String, Object> cookies;
    public Set<String> multiPart;
    public boolean logAll;
    public ContentType accept;
    public ContentType contentType;
    public Filter filter;
    public String proxy;

    public record Authorization(AUTHORIZATION_TYPES authorizationTypes, String userName, String passwordOrToken) {
    }

}
