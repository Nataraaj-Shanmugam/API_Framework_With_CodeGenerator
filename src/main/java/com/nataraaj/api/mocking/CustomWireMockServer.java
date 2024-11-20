package com.nataraaj.api.mocking;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.matching.EqualToPattern;
import com.github.tomakehurst.wiremock.matching.MultipartValuePatternBuilder;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

public class CustomWireMockServer {
    static WireMockServer wireMockServer;

    public static void enableMock() {
        // Configure WireMock server
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().port(7777));

        // Start the server
        wireMockServer.start();

        // Configure stubbed endpoints
        wireMockServer.stubFor(get(urlEqualTo("/testingCurl?query1=queryValue"))
                .withBasicAuth("Tester", "Testing")
                .withCookie("Cookie1", equalTo("cookie1Value"))
                .withRequestBody(matching(".*Content-Disposition: form-data; name=\"key1\".*value1.*"))
                .withRequestBody(matching(".*Content-Disposition: form-data; name=\"key2\".*"))
//                .withRequestBody(containing("key1=value1"))
//                .withRequestBody(containing("key2=value2"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/x-www-form-urlencoded")
                        .withStatus(200)
                        .withBody("{\"message\": \"Hello, this is a mocked response!\"}")));

    }

    private static void disableMock() {
        wireMockServer.stop();
    }

    public static void main(String[] args) {
        enableMock();
    }
}
