package com.exp.liveness.utils;

import com.exp.liveness.GenUnitTestData;
import com.exp.liveness.bean.RemoteService;
import com.exp.liveness.envm.RemoteServiceProtocol;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;

class HttpUtilsTest {

    private final static String CHARSET = "UTF-8";

    private final static String RESPONSE_BODY_OK = "/com/exp/liveness/utils/HttpUtilsTest-OK.json";

    private final static String RESPONSE_BODY_ERR = "/com/exp/liveness/utils/HttpUtilsTest-Error.json";

    private RemoteService remoteService;

    @BeforeEach
    void setUp() {
        this.remoteService = new RemoteService();
        remoteService.setName("UT_Service");
        remoteService.setProtocol(RemoteServiceProtocol.HTTP);
        remoteService.setAddress(GenUnitTestData.genURL());
    }

    @AfterEach
    void tearDown() {
    }

    @Test
    void testLivenessWithOK() throws Exception {
        String jsonAnyOK = IOUtils.toString(this.getClass().getResourceAsStream(RESPONSE_BODY_OK), CHARSET);
        CloseableHttpClient mockHttpClient = mockAnyHttpRequest(true, HttpStatus.SC_OK, jsonAnyOK, CHARSET);
        ReflectionTestUtils.invokeMethod(HttpUtils.class, "setMockHttpClient", mockHttpClient);

        boolean isOK = HttpUtils.testLiveness(this.remoteService);
        Assertions.assertTrue(isOK);
        Assertions.assertEquals(HttpStatus.SC_OK, this.remoteService.getStatusCode());
    }

    @Test
    void testLivenessWithError() throws Exception {
        String jsonAnyOK = IOUtils.toString(this.getClass().getResourceAsStream(RESPONSE_BODY_ERR), CHARSET);
        CloseableHttpClient mockHttpClient = mockAnyHttpRequest(true, HttpStatus.SC_INTERNAL_SERVER_ERROR, jsonAnyOK, CHARSET);
        ReflectionTestUtils.invokeMethod(HttpUtils.class, "setMockHttpClient", mockHttpClient);

        boolean isOK = HttpUtils.testLiveness(this.remoteService);
        Assertions.assertFalse(isOK);
        Assertions.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, this.remoteService.getStatusCode());
    }

    /**
     * ??????????????????????????? HTTP ??????
     * @param getORpost ???????????? true:HttpGet.class ??? false:HttpPost.class
     * @param httpStatus ????????????????????? HttpStatus
     * @param responseBody ???????????????????????? Entity
     * @return  mockHttpClient: ??????????????????????????? HttpClient ???????????? mockHttpClient ??????????????????
     * @throws Exception
     */
    private static CloseableHttpClient mockAnyHttpRequest(boolean getORpost, int httpStatus,
                                           String responseBody, String responseCharset) throws Exception {
        /* ?????? mock ?????? */
        CloseableHttpClient mockHttpClient = Mockito.mock(CloseableHttpClient.class);
        CloseableHttpResponse mockHttpResponse = Mockito.mock(CloseableHttpResponse.class);
        StatusLine mockStatusLine = Mockito.mock(StatusLine.class);

        /* ?????? mock ?????? */
        Mockito.when(mockHttpClient.execute(
                getORpost ? Mockito.any(HttpGet.class) : Mockito.any(HttpPost.class)
        )).thenReturn(mockHttpResponse);
        Mockito.when(mockHttpResponse.getStatusLine()).thenReturn(mockStatusLine);
        Mockito.when(mockStatusLine.getStatusCode()).thenReturn(httpStatus);
        Mockito.when(mockHttpResponse.getEntity()).thenReturn(
                new StringEntity(responseBody, StandardCharsets.UTF_8)
        );
        return mockHttpClient;
    }

}