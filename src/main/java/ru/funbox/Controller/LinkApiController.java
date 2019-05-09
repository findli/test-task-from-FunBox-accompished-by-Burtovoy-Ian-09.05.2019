package ru.funbox.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.funbox.Dto.VisitedLinksDto;
import ru.funbox.service.LinkService;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Set;

@Slf4j
@RestController
public class LinkApiController {

    private final LinkService linkService;

    public LinkApiController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping("/visited_links")
    public Mono<Object> visitedLinks(@RequestBody @Valid VisitedLinksDto visitedLinksDto) {
        return Mono.fromCallable(() -> {
            linkService.addLinks("link", visitedLinksDto.getLinks(), System.currentTimeMillis() / 1000L);

            final HashMap body = new HashMap();
            body.put("status", HttpStatus.OK);

            return body;
        });
    }

    @GetMapping("/visited_domains")
    public Mono<Object> visitedDomains(@RequestParam("from") final long from, @RequestParam("to") final long to) {
        return Mono.fromCallable(() -> {
            final Set links = linkService.getLinks("link", from, to);

            final HashMap<String, Object> body = new HashMap();
            body.put("domains", links);
            body.put("status", HttpStatus.OK);
            return body;
        });
    }
}

