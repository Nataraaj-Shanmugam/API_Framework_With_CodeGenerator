package com.nataraaj.api.generic;

import com.google.gson.Gson;
import com.nataraaj.api.generic.enums.AUTHORIZATION_TYPES;
import com.nataraaj.api.generic.model.RequestBuilder;
import io.restassured.config.HttpClientConfig;

import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CurlConverter {


    static HttpClientConfig httpClientConfig = HttpClientConfig.httpClientConfig();
    static boolean haveRequestBody = false;
    public static final StringBuilder requestStringBuilder = new StringBuilder("RequestBuilder.builder()"),
            testStringBuilder = new StringBuilder("@Test \n public void testMethod(){\n");
    static StringBuilder encodedFormBodyStringBuilder = null;
    static final Set<String> importStatements = new HashSet<>(0), multiPartData = new HashSet<>(0);
    static final Map<String, Map<String, Object>> data = new HashMap<>(0);

    public static void codeGenerator(String curl) {
        importStatements.add("import com.nataraaj.api.generic.APIHelper;\n" +
                "import com.nataraaj.api.generic.enums.DATA_FORMATS;\n" +
                "import com.nataraaj.api.generic.model.RequestBuilder;\n" +
                "import org.testng.annotations.Test;");

        requestGenerator(curl);
        testMethodGenerator();
        classFileGenerator();
    }

    private static void classFileGenerator() {
        StringBuilder builder = new StringBuilder();

        for (String importStatement : importStatements)
            builder.append(importStatement).append("\n");
        if (encodedFormBodyStringBuilder != null)
            requestStringBuilder.append(".body(\"").append(encodedFormBodyStringBuilder).append("\")");

        if (!multiPartData.isEmpty())
            requestStringBuilder.append(".multiPart(multiPartSet)");

        String fileValue = builder.append("\n").append("public class API_CodeGenerator_Generated {\n\n").append(testStringBuilder).append("\n RequestBuilder requestBuilder = ").append(requestStringBuilder).append(".build();\n")
                .append("\nnew APIHelper(DATA_FORMATS.JSON).hitAPI(requestBuilder).prettyPrint();\n").append("}\n}").toString();

        System.out.println(fileValue);
    }

    private static void testMethodGenerator() {
        for (String[] eachEntity : new String[][]{
                {"Headers", "headers", "headerMap"},
                {"Cookie", "cookies", "cookieMap"},
                {"QueryParam", "queryParameter", "queryMap"}}) {
            if (data.containsKey(eachEntity[0])) {
                Map<String, Object> entityMap = data.get(eachEntity[0]);
                Iterator<Map.Entry<String, Object>> mapIterator = entityMap.entrySet().iterator();
                Map.Entry<String, Object> entry;
                if (entityMap.size() == 1) {
                    entry = mapIterator.next();
                    importStatements.add("import java.util.Collections;");
                    requestStringBuilder.append(".").append(eachEntity[1]).append("(Collections.singletonMap(\"").append(entry.getKey()).append("\" , \"").append(entry.getValue()).append("\"))");
                } else {
                    importStatements.add("import java.util.HashMap;");
                    testStringBuilder.append("\nHashMap<String,Object> ").append(eachEntity[2]).append(" = new HashMap<>();\n");
                    while (mapIterator.hasNext()) {
                        entry = mapIterator.next();
                        testStringBuilder.append(eachEntity[2]).append(".put(\"").append(entry.getKey()).append("\" , \"").append(entry.getValue()).append("\");\n");
                    }
                    requestStringBuilder.append(".").append(eachEntity[1]).append("(").append(eachEntity[2]).append(")");
                }
            }
        }


        if (!multiPartData.isEmpty()) {
            importStatements.add("import java.util.HashSet;");
            testStringBuilder.append("\nHashSet<String> multiPartSet = new HashSet<>();\n");
            for (String eachMultiPart : multiPartData)
                testStringBuilder.append("multiPartSet.add(\"").append(eachMultiPart).append("\");\n");
        }
    }

    private static void requestGenerator(String curl) {
        String hitAPIDetails = "", url = "";

        Matcher urlMatcher = Pattern.compile("'http.?://[^\\s ]+").matcher(curl);
        if (urlMatcher.find()) {
            url = urlMatcher.group();
            curl = curl.replace(url + " ", "");
            url = url.replace("'", "");
        } else {
            System.out.println("No URL found in the text.");
        }

        Matcher curlMatcher = Pattern.compile("(?<flag>--?[A-Za-z\\-]+)(?:\\s+(?<value>\"[^\"]*\"|'[^']*'|[^-\\s][^\\s]*))?").matcher(curl.trim());

        while (curlMatcher.find()) {
            String command = curlMatcher.group("flag");
            String value = curlMatcher.group("value");
            if (command.equals("-X") || command.equals("--request"))
                hitAPIDetails = value;
            else
                convertCurlToJavaCode(command, value);
        }

        Map<String, ?> httpParams = httpClientConfig.params();
        if (httpParams.containsKey("http.connection.timeout") || httpParams.containsKey("http.socket.timeout")) {
            importStatements.add("import io.restassured.config.HttpClientConfig;");
            String httpConfig = httpParams.containsKey("http.connection.timeout") ? ".setParam(\"http.connection.timeout\", " + httpParams.get("http.connection.timeout") + ")" : "";
            httpConfig += httpParams.containsKey("http.socket.timeout") ? ".setParam(\"http.socket.timeout\", " + httpParams.get("http.socket.timeout") + ")" : "";
            requestStringBuilder.append(".httpsConfig(HttpClientConfig.httpClientConfig()").append(httpConfig).append(")");
        }


        String[] urlDetails = url.split("\\?");
        if (urlDetails.length > 1) {
            for (String queryParam : urlDetails[1].split("&")) {
                updateEntity("QueryParam", queryParam);
            }
        }

        URI uri = URI.create(urlDetails[0]);
        if (hitAPIDetails.isEmpty())
            hitAPIDetails = haveRequestBody ? "POST" : "GET";

        requestStringBuilder.append(".methodType(Method.")
                .append(hitAPIDetails).append(")")
                .append(".baseURI")
                .append("(\"")
                .append(uri.getScheme())
                .append("://")
                .append(uri.getAuthority())
                .append("\")")
                .append(".url(\"")
                .append(uri.getPath())
                .append("\")");

        importStatements.add("import io.restassured.http.Method;");
    }

    public static void updateEntity(String type, String value) {
        Map<String, Object> headerData = data.getOrDefault(type, new HashMap<>());
        String[] tempArray = type.equals("QueryParam") ? value.split("=") : value.replaceAll("[\"']+", "").split(":");
        headerData.put(tempArray[0].trim(), tempArray[1].trim());
        data.put(type, headerData);
    }

    private static <T> void convertCurlToJavaCode(String command, String value) {
        switch (command) {
            case "-H":
            case "--header":
                updateEntity("Headers", value);
                break;
            case "--connect-timeout":
                httpClientConfig = httpClientConfig.setParam("http.connection.timeout", Integer.parseInt(value.trim()) * 1000);
                break;
            case "--max-time":
                httpClientConfig = httpClientConfig.setParam("http.socket.timeout", Integer.parseInt(value.trim()) * 1000);
                break;
            case "-k":
            case "--insecure":
                requestStringBuilder.append(".relaxRequest(true)");
                break;
            case "--data":
            case "--d":
            case "--data-raw":
                haveRequestBody = true;
                requestStringBuilder.append(".body(").append(new Gson().toJson(value.replaceAll("\n", "").replaceAll("'", ""))).append(")");
                break;
            case "--data-urlencode":
                if (encodedFormBodyStringBuilder == null)
                    encodedFormBodyStringBuilder = new StringBuilder();
                else encodedFormBodyStringBuilder.append("&");
                haveRequestBody = true;
                encodedFormBodyStringBuilder.append(value.replaceAll("'", ""));
                break;
            case "-b":
                updateEntity("Cookie", value);
                break;
            case "-F":
            case "-T":
            case "--form":
                multiPartData.add(value.replaceAll("[\"']", ""));
                break;
            case "-v":
                requestStringBuilder.append(".logAll(true)");
                break;
            case "-x":
                requestStringBuilder.append(".proxy(\"").append(value.replaceAll("[\"']+", "")).append("\")");
                break;
            case "-e":
                updateEntity("Headers", "Referer: " + value.trim());
                break;
            case "-L":
            case "--location":
                requestStringBuilder.append(".allowRedirects(true)");
                break;
            case "-u":
            case "--user":
                String[] tempArray = value.split(":");
                RequestBuilder.builder().authorization(new RequestBuilder.Authorization(AUTHORIZATION_TYPES.BASIC, "", ""));
                requestStringBuilder.append(".authorization(new RequestBuilder.Authorization(").append("AUTHORIZATION_TYPES.BASIC").append(" , ").append(tempArray[0].trim()).append(" , ").append(tempArray[1].trim()).append(")");
                break;
            default:
                System.out.println("Not supported attributed " + command);
                break;
        }
    }
}
