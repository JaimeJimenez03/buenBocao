package com.buenbocao.api.social.repository;

import com.buenbocao.api.social.entity.BrowsingHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BrowsingHistoryRepository extends JpaRepository<BrowsingHistory, Long> {

    List<BrowsingHistory> findTop3ByUserIdOrderByCreatedAtDesc(Long userId);

    List<BrowsingHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    @Modifying
    @Query("DELETE FROM BrowsingHistory b WHERE b.user.id = :userId AND b.content = :content")
    void deleteByUserIdAndContent(@Param("userId") Long userId, @Param("content") String content);
}