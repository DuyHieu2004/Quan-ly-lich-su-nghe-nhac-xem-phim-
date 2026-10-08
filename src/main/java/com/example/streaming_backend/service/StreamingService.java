package com.example.streaming_backend.service;

import com.example.streaming_backend.model.HistoryLog;
import com.example.streaming_backend.repository.HistoryLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Service
public class StreamingService {

    @Autowired
    private HistoryLogRepository historyLogRepository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String TRENDING_KEY = "trending:media_all_time";

    public HistoryLog logListenEvent(String userId, String mediaId, String mediaType, int stoppedAtSecond) {
        // 1. Lưu trực tiếp vào Cassandra (Heavy Write)
        HistoryLog log = new HistoryLog(userId, Instant.now(), mediaId, mediaType, stoppedAtSecond);
        historyLogRepository.save(log);

        // 2. Cập nhật Trending trên Redis (Real-time update)
        // Nếu bài hát chưa có trong tập hợp, Redis sẽ thêm mới với điểm là 1. Nếu có rồi thì cộng thêm 1.
        redisTemplate.opsForZSet().incrementScore(TRENDING_KEY, mediaId, 1);
        
        return log;
    }

    public List<HistoryLog> getUserHistory(String userId) {
        return historyLogRepository.findByUserId(userId);
    }

    public Set<ZSetOperations.TypedTuple<String>> getTopTrending(int limit) {
        // Lấy danh sách Top Trending cùng với số điểm (số lượt nghe) từ cao xuống thấp
        return redisTemplate.opsForZSet().reverseRangeWithScores(TRENDING_KEY, 0, limit - 1);
    }
}
