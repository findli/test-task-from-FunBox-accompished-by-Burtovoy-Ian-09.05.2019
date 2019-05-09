package ru.funbox.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import ru.funbox.SpringBootRestApplication;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = SpringBootRestApplication.class)
public class LinkServiceTest {
    @Autowired
    LinkService linkService;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    RedisTemplate redisTemplate;

    @Test
    public void LinkServiceTest_AddLinks() throws JsonProcessingException {
        final String key = "link";
        final String[] links = {"url1", "url2"};
        final long score = System.currentTimeMillis() / 1000L;
        linkService.addLinks(key, links, score);

        final Set link = redisTemplate.opsForZSet().rangeByScore(key, score - 1, score + 10);
        assertEquals(new HashSet(Arrays.asList(links)), link);
    }

    @Test
    public void LinkServiceTest_RetrieveLinks() throws JsonProcessingException {
        final String key = "link";
        final String[] links = {"url1", "url2"};
        final long score = System.currentTimeMillis() / 1000L;
        linkService.addLinks(key, links, score);

        final Set link = linkService.getLinks(key, score - 1, score + 10);
        assertEquals(new HashSet(Arrays.asList(links)), link);
    }
}
