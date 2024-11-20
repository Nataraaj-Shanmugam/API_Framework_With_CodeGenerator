import com.nataraaj.api.generic.APIHelper;
import com.nataraaj.api.generic.CurlConverter;
import com.nataraaj.api.generic.CurlConverter_Updated;
import com.nataraaj.api.generic.enums.AUTHORIZATION_TYPES;
import com.nataraaj.api.generic.enums.DATA_FORMATS;
import com.nataraaj.api.generic.model.RequestBuilder;
import io.restassured.config.HttpClientConfig;
import io.restassured.http.Method;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.HashMap;

public class FrameworkTest {
    static String[] curlArray = {"curl --location --request PATCH 'https://reqres.in/api/users/2' \\\n" +
            "--header 'Content-Type: application/json' \\\n" +
            "--header 'Accept: application/json' \\\n" +
            "-x \"http://proxyserver:8080\" \\\n" +
            "--data '{\n" +
            "    \"name\": \"morpheus\",\n" +
            "    \"job\": \"zion resident\",\n" +
            "    \"updatedAt\": \"2024-10-30T09:29:39.434Z\"\n" +
            "}'" +
            "  --connect-timeout 10 \\\n" +
            "  --max-time 30 \\\n" +
            "  -v",

            "curl --location --request PUT 'https://jsonplaceholder.typicode.com/posts/1' \\\n" +
                    "--header 'Content-Type: application/json' \\\n" +
                    "--data '{\"id\": 1, \"title\": \"foo\", \"body\": \"bar\", \"userId\": 1}'",
            "curl --location --request GET 'https://jsonplaceholder.typicode.com/comments?postId=1' \\\n" +
                    "--header 'Content-Type: application/json' \\\n" +
                    "--data '{\"id\": 1, \"title\": \"foo\", \"body\": \"bar\", \"userId\": 1}'",
            //form data
            "curl 'https://httpbingo.org/post' \\\n" +
                    "  -H 'accept: text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7' \\\n" +
                    "  -H 'accept-language: en-US,en;q=0.9' \\\n" +
                    "  --data-raw 'custname=dsfsdf&custtel=sdfsdf&custemail=sdfsfs%40gmail.com&size=small&topping=cheese&delivery=&comments='",
            //cookie
            "curl --user name:password http://www.example.com"
    };

    public static void main(String[] args) throws MalformedURLException {
        CurlConverter.codeGenerator("curl --location --request GET 'http://localhost:7777/testingCurl?query1=queryValue' \\\n" +
                "--header 'Authorization: Basic VGVzdGVyOlRlc3Rpbmc=' \\\n" +
                "--header 'Cookie: Cookie1=cookie1Value' \\\n" +
                "--form 'key1=\"value1\"' \\\n" +
                "--form 'key2=@\"/Users/nataraaj/Downloads/_3.jpeg\"'");


//        HashMap<String, Object> form = new HashMap<>();
//        form.put("Form1", "formValue1");
//        form.put("Form2", "formValue3");
//        RequestBuilder rb = RequestBuilder.builder().body("Form1=formValue1&Form2=formValue2").baseURI("http://localhost:7777/testingCurl").queryParameter(Collections.singletonMap("query1", "queryValue")).cookies(Collections.singletonMap("Cookie1", "cookie1Value")).headers(Collections.singletonMap("Content-Type", "application/x-www-form-urlencoded")).methodType(Method.GET).authorization(new RequestBuilder.Authorization(AUTHORIZATION_TYPES.BASIC, "Tester", "Testing")).build();
//        new APIHelper(DATA_FORMATS.JSON).hitAPI(rb).prettyPrint();

//        HashMap<String, Object> form = new HashMap<>();
//        form.put("key1", "value1");
//        form.put("key2", "value2");
//
//        new APIHelper(DATA_FORMATS.JSON).hitAPI(RequestBuilder.builder().baseURI("https://reqbin.com/echo/post/form").formData().build()).prettyPrint();

    }
}
