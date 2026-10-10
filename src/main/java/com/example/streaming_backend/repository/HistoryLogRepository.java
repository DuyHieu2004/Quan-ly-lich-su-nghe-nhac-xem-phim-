package com.example.streaming_backend.repository;

import com.example.streaming_backend.model.HistoryLog;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.data.cassandra.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface HistoryLogRepository extends CassandraRepository<HistoryLog, String> {
    List<HistoryLog> findByUserId(String userId);

    // Q1. Range Scan: Tìm lịch sử trong khoảng thời gian (Dùng trên Clustering Column)
    @Query("SELECT * FROM history_logs WHERE user_id = ?0 AND event_time >= ?1 AND event_time <= ?2")
    List<HistoryLog> findByUserIdAndEventTimeBetween(String userId, Instant startTime, Instant endTime);

    // Q2. Aggregation: Đếm tổng số hoạt động của user
    @Query("SELECT COUNT(*) FROM history_logs WHERE user_id = ?0")
    long countTotalActivitiesByUserId(String userId);

    // Q3. Limit & Ordering: Lấy N hoạt động gần nhất của user
    @Query("SELECT * FROM history_logs WHERE user_id = ?0 LIMIT ?1")
    List<HistoryLog> findRecentLogsByUserId(String userId, int limit);

    // Q4. ALLOW FILTERING: Lọc các hoạt động bị bỏ dở (stopped_at_second > 0)
    @Query("SELECT * FROM history_logs WHERE user_id = ?0 AND stopped_at_second > 0 ALLOW FILTERING")
    List<HistoryLog> findUnfinishedActivities(String userId);

    // Q5. ALLOW FILTERING trên media_id: Xem user có nghe bài này chưa
    @Query("SELECT * FROM history_logs WHERE user_id = ?0 AND media_id = ?1 LIMIT 1 ALLOW FILTERING")
    HistoryLog findLatestLogByUserAndMedia(String userId, String mediaId);

    // Q6. Query Index: Lọc dữ liệu theo loại media (Cần tạo Index trên db trước)
    @Query("SELECT * FROM history_logs WHERE media_type = ?0 LIMIT ?1")
    List<HistoryLog> findByMediaType(String mediaType, int limit);
}
