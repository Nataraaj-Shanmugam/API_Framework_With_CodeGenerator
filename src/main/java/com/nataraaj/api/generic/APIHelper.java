package com.nataraaj.api.generic;

import com.nataraaj.api.generic.enums.DATA_FORMATS;
import com.nataraaj.api.generic.model.RequestBuilder;
import io.restassured.RestAssured;
import io.restassured.config.RestAssuredConfig;
import io.restassured.internal.multipart.MultiPartSpecificationImpl;
import io.restassured.response.Response;
import io.restassured.specification.MultiPartSpecification;
import io.restassured.specification.RequestSpecification;
import org.testng.Assert;

import java.io.File;

public class APIHelper {
    private final DATA_FORMATS dataFormats;

    public APIHelper(DATA_FORMATS dataFormats) {
        this.dataFormats = dataFormats;
    }

    public Response hitAPI(RequestBuilder requestBuilder) {
        RequestSpecification requestSpecification = RestAssured.given().baseUri(requestBuilder.baseURI);

        if (requestBuilder.relaxRequest) {
            requestSpecification.relaxedHTTPSValidation();
        }

        if (requestBuilder.headers != null) {
            requestSpecification.headers(requestBuilder.headers);
        }
        if (requestBuilder.queryParameter != null) {
            requestSpecification.queryParams(requestBuilder.queryParameter);
        }

        if (requestBuilder.pathParameter != null) {
            requestSpecification.pathParams(requestBuilder.pathParameter);
        }

        if (requestBuilder.formData != null) {
            requestSpecification.formParams(requestBuilder.formData);
        }

        if (requestBuilder.body != null) {
            requestSpecification.body(requestBuilder.body);
        }

        if (requestBuilder.authorization != null) {
            RequestBuilder.Authorization authorization = requestBuilder.authorization;
            switch (authorization.authorizationTypes()) {
                case OAUTH2 -> requestSpecification.auth().preemptive().oauth2(authorization.passwordOrToken());
                case BASIC ->
                        requestSpecification.auth().preemptive().basic(authorization.userName(), authorization.passwordOrToken());
                default -> throw new RuntimeException("Non Supported Auth type");
            }
        }

        if (requestBuilder.allowRedirects) {
            requestSpecification.redirects().follow(true);
        }

        if (requestBuilder.httpsConfig != null) {
            requestSpecification.config(RestAssuredConfig.config().httpClient(requestBuilder.httpsConfig));
        }

        if (requestBuilder.cookies != null) {
            requestSpecification.cookies(requestBuilder.cookies);
        }

        if (requestBuilder.multiPart != null) {
            for (String eachMultipart : requestBuilder.multiPart) {
                String[] data = eachMultipart.split("=");
                if (data.length > 1) {
                    if (data[1].startsWith("@"))
                        requestSpecification.multiPart(data[0], new File(data[1].substring(1)));
                    else
                        requestSpecification.multiPart(data[0], data[1]);
                } else {
                    requestSpecification.multiPart(new File(data[0]));
                }
            }
        }

        if (requestBuilder.logAll) {
            requestSpecification.log().all();
        }
        if (requestBuilder.accept != null) {
            requestSpecification.accept(requestBuilder.accept);
        }
        if (requestBuilder.contentType != null) {
            requestSpecification.contentType(requestBuilder.contentType);
        }

        if (requestBuilder.filter != null) {
            requestSpecification.filter(requestBuilder.filter);
        }

        Response response = switch (requestBuilder.methodType) {
            case GET ->
                    requestBuilder.url == null || requestBuilder.url.isEmpty() ? requestSpecification.get() : requestSpecification.get(requestBuilder.url);
            case PUT ->
                    requestBuilder.url == null || requestBuilder.url.isEmpty() ? requestSpecification.put() : requestSpecification.put(requestBuilder.url);
            case POST ->
                    requestBuilder.url == null || requestBuilder.url.isEmpty() ? requestSpecification.post() : requestSpecification.post(requestBuilder.url);
            case DELETE ->
                    requestBuilder.url == null || requestBuilder.url.isEmpty() ? requestSpecification.delete() : requestSpecification.delete(requestBuilder.url);
            case HEAD ->
                    requestBuilder.url == null || requestBuilder.url.isEmpty() ? requestSpecification.head() : requestSpecification.head(requestBuilder.url);
            case OPTIONS ->
                    requestBuilder.url == null || requestBuilder.url.isEmpty() ? requestSpecification.options() : requestSpecification.options(requestBuilder.url);
            case PATCH ->
                    requestBuilder.url == null || requestBuilder.url.isEmpty() ? requestSpecification.patch() : requestSpecification.patch(requestBuilder.url);
            default -> throw new RuntimeException("Non Supported Method type");
        };


        if (requestBuilder.assertStatusCode > 99) {
            Assert.assertEquals(response.statusCode(), requestBuilder.assertStatusCode);
        }

        return response;
    }


}
