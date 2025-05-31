package com.ceyway.ceyway.travelplanner.repository;

import com.ceyway.ceyway.travelplanner.model.KnowledgeBase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface KnowledgeBaseRepository extends JpaRepository<KnowledgeBase, Long> {

    @Query(value = "SELECT * FROM knowledge_base WHERE content ILIKE %:query%", nativeQuery = true)
    List<KnowledgeBase> searchByText(@Param("query") String query);
}
