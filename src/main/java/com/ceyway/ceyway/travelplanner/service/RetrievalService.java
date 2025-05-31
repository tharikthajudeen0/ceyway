package com.ceyway.ceyway.travelplanner.service;

import com.ceyway.ceyway.travelplanner.model.KnowledgeBase;
import com.ceyway.ceyway.travelplanner.repository.KnowledgeBaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RetrievalService {
    private final KnowledgeBaseRepository kbRepo;

    public RetrievalService(KnowledgeBaseRepository kbRepo) {
        this.kbRepo = kbRepo;
    }

    public String retrieveContext(String query) {
        List<KnowledgeBase> results = kbRepo.searchByText(query);  // or use embedding search
        return results.stream()
                .map(KnowledgeBase::getContent)
                .limit(3)
                .collect(Collectors.joining("\n---\n"));
    }
}
