package com.ceyway.ceyway.travelplanner.controller;

import com.ceyway.ceyway.travelplanner.service.RetrievalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final RetrievalService retrievalService;

    public SearchController(RetrievalService retrievalService) {
        this.retrievalService = retrievalService;
    }

    @GetMapping
    public ResponseEntity<String> search(@RequestParam String query) {
        String context = retrievalService.retrieveContext(query);
        return ResponseEntity.ok(context);
    }
}
