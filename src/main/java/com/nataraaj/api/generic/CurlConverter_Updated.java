package com.nataraaj.api.generic;

import io.restassured.config.HttpClientConfig;

import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

public class CurlConverter_Updated {

    private static final HttpClientConfig httpClientConfig = HttpClientConfig.httpClientConfig();
    private static boolean hasRequestBody = false;
    private static final StringBuilder requestBuilderString = new StringBuilder("RequestBuilder.builder()");
    private static final StringBuilder testMethodString = new StringBuilder("@Test\npublic void testMethod() {\n");
    private static StringBuilder encodedFormBody = null;
    private static final Set<String> importStatements = new LinkedHashSet<>();
    private static final Set<String> multipartData = new LinkedHashSet<>();
    private static final Map<String, Map<String, Object>> entityData = new HashMap<>();

    public static void generateCodeFromCurl(String curl) {
        addDefaultImports();
        parseCurlCommand(curl);
        generateTestMethod();
        generateClassFile();
    }

    private static void addDefaultImports() {
        importStatements.addAll(List.of(
                "import com.nataraaj.api.generic.model.RequestBuilder;",
                "import org.testng.annotations.Test;",
                "import java.util.*;"
        ));
    }

    private static void generateClassFile() {
        var builder = new StringBuilder();

        // Add imports
        importStatements.forEach(importStatement -> builder.append(importStatement).append("\n"));

        // Handle body and multipart data
        if (encodedFormBody != null) {
            requestBuilderString.append(".body(\"").append(encodedFormBody).append("\")");
        }
        if (!multipartData.isEmpty()) {
            requestBuilderString.append(".multiPart(multipartSet)");
        }

        // Generate class content
        var classContent = builder.append("\n")
                .append("public class API_CodeGenerator_Generated {\n\n")
                .append(testMethodString)
                .append("RequestBuilder requestBuilder = ").append(requestBuilderString).append(".build();\n")
                .append("new APIHelper(DATA_FORMATS.JSON).hitAPI(requestBuilder).prettyPrint();\n")
                .append("}\n}")
                .toString();

        System.out.println(classContent);
    }

    private static void generateTestMethod() {
        var entities = new String[][]{
                {"Headers", "headers", "headerMap"},
                {"Cookie", "cookies", "cookieMap"},
                {"QueryParam", "queryParameter", "queryMap"}
        };

        for (var entity : entities) {
            if (entityData.containsKey(entity[0])) {
                var map = entityData.get(entity[0]);
                if (map.size() == 1) {
                    var entry = map.entrySet().iterator().next();
                    importStatements.add("import java.util.Collections;");
                    requestBuilderString.append(".").append(entity[1])
                            .append("(Collections.singletonMap(\"")
                            .append(entry.getKey()).append("\", \"")
                            .append(entry.getValue()).append("\"))");
                } else {
                    importStatements.add("import java.util.HashMap;");
                    testMethodString.append("\nvar ").append(entity[2]).append(" = new HashMap<String, Object>();\n");
                    map.forEach((key, value) -> testMethodString.append(entity[2])
                            .append(".put(\"").append(key).append("\", \"")
                            .append(value).append("\");\n"));
                    requestBuilderString.append(".").append(entity[1]).append("(").append(entity[2]).append(")");
                }
            }
        }

        if (!multipartData.isEmpty()) {
            importStatements.add("import java.util.HashSet;");
            testMethodString.append("\nvar multipartSet = new HashSet<String>();\n");
            multipartData.forEach(data -> testMethodString.append("multipartSet.add(\"").append(data).append("\");\n"));
        }
    }

    private static void parseCurlCommand(String curl) {
        var urlMatcher = Pattern.compile("'http.?://[^\\s]+'").matcher(curl);
        var url = urlMatcher.find() ? urlMatcher.group().replace("'", "") : null;

        if (url == null) {
            System.out.println("No valid URL found in the cURL command.");
            return;
        }

        curl = curl.replace(url + " ", "");
        var urlDetails = url.split("\\?");
        if (urlDetails.length > 1) {
            for (var param : urlDetails[1].split("&")) {
                updateEntityData("QueryParam", param);
            }
        }

        var uri = URI.create(urlDetails[0]);
        var method = hasRequestBody ? "POST" : "GET";
        requestBuilderString.append(".methodType(Method.").append(method).append(")")
                .append(".baseURI(\"").append(uri.getScheme()).append("://").append(uri.getAuthority()).append("\")")
                .append(".url(\"").append(uri.getPath()).append("\")");

        importStatements.add("import io.restassured.http.Method;");

        var matcher = Pattern.compile("(?<flag>--?[A-Za-z\\-]+)(?:\\s+(?<value>\"[^\"]*\"|'[^']*'|[^-\\s][^\\s]*))?").matcher(curl);
        while (matcher.find()) {
            var command = matcher.group("flag");
            var value = matcher.group("value");
//            if (command.equals("-X") || command.equals("--request"))
//                hitAPIDetails = value;
//            else
            convertCurlFlagToRequest(command, value);
        }
    }

    private static void convertCurlFlagToRequest(String flag, String value) {
        switch (flag) {
            case "-H", "--header" -> updateEntityData("Headers", value);
            case "--connect-timeout" ->
                    httpClientConfig.setParam("http.connection.timeout", Integer.parseInt(value.trim()) * 1000);
            case "--max-time" ->
                    httpClientConfig.setParam("http.socket.timeout", Integer.parseInt(value.trim()) * 1000);
            case "-k", "--insecure" -> requestBuilderString.append(".relaxRequest(true)");
            case "--data", "-d", "--data-raw" -> {
                hasRequestBody = true;
                requestBuilderString.append(".body(new Gson().toJson(").append(value.replace("'", "")).append("))");
            }
            case "--data-urlencode" -> {
                if (encodedFormBody == null) {
                    encodedFormBody = new StringBuilder();
                } else {
                    encodedFormBody.append("&");
                }
                encodedFormBody.append(value.replace("'", ""));
            }
            case "-b", "--cookie" -> updateEntityData("Cookie", value);
            case "-F", "--form" -> multipartData.add(value.replaceAll("[\"']", ""));
            case "--location" -> requestBuilderString.append(".allowRedirects(true)");
            default -> System.out.println("Unsupported cURL flag: " + flag);
        }
    }

    private static void updateEntityData(String type, String value) {
        var entityMap = entityData.computeIfAbsent(type, k -> new HashMap<>());
        var keyValue = value.split("=");
        entityMap.put(keyValue[0].trim(), keyValue[1].trim());
    }
}
