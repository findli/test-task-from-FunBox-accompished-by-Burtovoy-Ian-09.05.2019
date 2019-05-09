package ru.funbox.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class LinkService {

    @Autowired
    RedisTemplate redisTemplate;

    public void addLinks(final String key, final String[] links, final double score) {
        for (String link : links) redisTemplate.opsForZSet().add(key, link, score);
    }

    public void addLinks(final String key, final List<String> links, final double score) {
        String[] array = new String[links.size()];
        System.arraycopy(links.toArray(), 0, array, 0, links.size());

        addLinks(key, array, score);
    }

    public Set getLinks(final String key, final long fromTs, final long toTs) {
        final Set link = redisTemplate.opsForZSet().rangeByScore(key, fromTs, toTs);

        return link;
    }

}
