package ru.funbox.Controller;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.EntityExchangeResult;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.funbox.Dto.VisitedLinksDto;
import ru.funbox.service.LinkService;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class IntegrationLinkTest {
    @Autowired
    public WebTestClient client;
    @Autowired
    LinkService linkService;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    RedisTemplate redisTemplate;
    final String[] links = {"http://url1.com", "http://url2.com"};
    final String key = "link";
    final long score = System.currentTimeMillis() / 1000L;

    @BeforeEach
    public void beforeEvery() {
        redisTemplate.delete("link");


        redisTemplate.opsForZSet().add(key, links[0], score);
        redisTemplate.opsForZSet().add(key, links[1], score);
    }

    @Test
    public void LinkServiceTest_AddLinks() {

        client.post()
                .uri("visited_links")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .body(Mono.just(new VisitedLinksDto(new ArrayList<String>() {{
                    add("http://url1.com");
                    add("http://url2.com");
                }})), VisitedLinksDto.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.status")
                .isEqualTo(HttpResponseStatus.OK.reasonPhrase());

        final Set link = redisTemplate.opsForZSet().rangeByScore(key, score - 1, score + 10);
        assertEquals(new HashSet(Arrays.asList(links)), link);
    }

    @Test
    public void LinkServiceTest_RetrieveLinks() throws IOException {
        final ObjectMapper objectMapper = new ObjectMapper();
        final EntityExchangeResult<byte[]> result = client.get()
                .uri("visited_domains?from=0&to=99999999999")
                .accept(MediaType.APPLICATION_JSON_UTF8)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON_UTF8)
                .expectBody()
                .jsonPath("$.domains")
                .isNotEmpty()
                .jsonPath("$.status")
                .isEqualTo(HttpResponseStatus.OK.reasonPhrase())
                .returnResult();

        final byte[] responseBody = result.getResponseBody();
        JsonNode s = objectMapper.readTree(responseBody);
        final JsonNode domains = s.get("domains");
        assertEquals("[\"http://url1.com\",\"http://url2.com\"]", domains.toString());
    }
}
