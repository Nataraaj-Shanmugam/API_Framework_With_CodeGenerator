import java.util.Collections;
import java.util.HashSet;

import io.restassured.http.Method;
import com.nataraaj.api.generic.APIHelper;
import com.nataraaj.api.generic.enums.DATA_FORMATS;
import com.nataraaj.api.generic.model.RequestBuilder;
import org.testng.annotations.Test;

import java.util.HashMap;

public class API_CodeGenerator_Generated {

    @Test
    public void testMethod() {

        HashMap<String, Object> headerMap = new HashMap<>();
        headerMap.put("Authorization", "Basic VGVzdGVyOlRlc3Rpbmc=");
        headerMap.put("Cookie", "Cookie1=cookie1Value");

        HashSet<String> multiPartSet = new HashSet<>();
        multiPartSet.add("key2=@/Users/nataraaj/Downloads/_3.jpeg");
        multiPartSet.add("key1=value1");

        RequestBuilder requestBuilder = RequestBuilder.builder().allowRedirects(true).methodType(Method.GET).baseURI("http://localhost:7777").url("/testingCurl").headers(headerMap).queryParameter(Collections.singletonMap("query1", "queryValue")).multiPart(multiPartSet).build();

        new APIHelper(DATA_FORMATS.JSON).hitAPI(requestBuilder).prettyPrint();
    }
}