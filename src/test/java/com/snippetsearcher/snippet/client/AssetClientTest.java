package com.snippetsearcher.snippet.client;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

class AssetClientTest {

  private RestTemplate restTemplate;
  private MockRestServiceServer server;
  private AssetClient client;

  @BeforeEach
  void setUp() {
    restTemplate = new RestTemplate();
    server = MockRestServiceServer.bindTo(restTemplate).build();

    client = new AssetClient("http://asset-service"); // baseUrl dummy
    // reemplazamos el RestTemplate interno para poder testear
    ReflectionTestUtils.setField(client, "restTemplate", restTemplate);
    ReflectionTestUtils.setField(client, "baseUrl", "http://asset-service");
  }

  @Test
  void uploadSnippet_usesProvidedContentType_andReturnsAssetKey() {
    byte[] content = "hello".getBytes(StandardCharsets.UTF_8);

    server
        .expect(requestTo("http://asset-service/v1/asset/snippets/abc.ps"))
        .andExpect(method(PUT))
        .andExpect(header(HttpHeaders.CONTENT_TYPE, "text/plain"))
        .andRespond(withStatus(HttpStatus.OK));

    String assetKey = client.uploadSnippet("snippets", "abc.ps", content, "text/plain");

    assertEquals("snippets/abc.ps", assetKey);
    server.verify();
  }

  @Test
  void uploadSnippet_whenContentTypeNull_usesOctetStream() {
    byte[] content = "hello".getBytes(StandardCharsets.UTF_8);

    server
        .expect(requestTo("http://asset-service/v1/asset/snippets/abc.ps"))
        .andExpect(method(PUT))
        .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
        .andRespond(withStatus(HttpStatus.OK));

    String assetKey = client.uploadSnippet("snippets", "abc.ps", content, null);

    assertEquals("snippets/abc.ps", assetKey);
    server.verify();
  }

  @Test
  void uploadSnippet_whenContentTypeBlank_usesOctetStream() {
    byte[] content = "hello".getBytes(StandardCharsets.UTF_8);

    server
        .expect(requestTo("http://asset-service/v1/asset/snippets/abc.ps"))
        .andExpect(method(PUT))
        .andExpect(header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
        .andRespond(withStatus(HttpStatus.OK));

    String assetKey = client.uploadSnippet("snippets", "abc.ps", content, "  ");

    assertEquals("snippets/abc.ps", assetKey);
    server.verify();
  }

  @Test
  void uploadSnippet_when4xx_restTemplateThrowsHttpClientErrorException() {
    server
        .expect(requestTo("http://asset-service/v1/asset/snippets/abc.ps"))
        .andExpect(method(PUT))
        .andRespond(withBadRequest());

    assertThrows(
        HttpClientErrorException.class,
        () -> client.uploadSnippet("snippets", "abc.ps", new byte[] {1, 2, 3}, "text/plain"));

    server.verify();
  }

  @Test
  void downloadSnippet_whenBlankKey_throwsIllegalArgument() {
    assertThrows(IllegalArgumentException.class, () -> client.downloadSnippet(" "));
    assertThrows(IllegalArgumentException.class, () -> client.downloadSnippet(null));
  }

  @Test
  void downloadSnippet_success_returnsBody() {
    byte[] body = "file".getBytes(StandardCharsets.UTF_8);

    server
        .expect(requestTo("http://asset-service/v1/asset/snippets/abc.ps"))
        .andExpect(method(GET))
        .andRespond(withSuccess(body, MediaType.APPLICATION_OCTET_STREAM));

    byte[] res = client.downloadSnippet("snippets/abc.ps");

    assertArrayEquals(body, res);
    server.verify();
  }

  @Test
  void downloadSnippet_when4xx_restTemplateThrowsHttpClientErrorException() {
    server
        .expect(requestTo("http://asset-service/v1/asset/snippets/abc.ps"))
        .andExpect(method(GET))
        .andRespond(withStatus(HttpStatus.NOT_FOUND));

    assertThrows(HttpClientErrorException.class, () -> client.downloadSnippet("snippets/abc.ps"));

    server.verify();
  }
}
