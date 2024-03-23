package com.gameverse.scoremaster.repository;

import com.gameverse.scoremaster.entity.ScoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScoreRepository extends JpaRepository<ScoreEntity, Long> {
    @Query(value = "select * from score s WHERE s.user_id = :userId", nativeQuery = true)
    Optional<List<ScoreEntity>> getScoreEntity(long userId);
}
