package com.example.streaming_backend.controller;

import com.example.streaming_backend.model.HistoryLog;
import com.example.streaming_backend.service.StreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/streaming")
@CrossOrigin(origins = "*") // Cho phép Front-end (như Angular) gọi API
public class StreamingController {

    @Autowired
    private StreamingService streamingService;

    // API nhận tín hiệu báo đã nghe nhạc từ Front-end (Ví dụ: gọi mỗi 10 giây hoặc khi stop bài)
    @PostMapping("/log")
    public ResponseEntity<HistoryLog> logEvent(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String mediaId = (String) payload.get("mediaId");
        String mediaType = (String) payload.get("mediaType");
        Integer stoppedAt = (Integer) payload.get("stoppedAtSecond");

        HistoryLog log = streamingService.logListenEvent(userId, mediaId, mediaType, stoppedAt);
        return ResponseEntity.ok(log);
    }

    // API xem lịch sử của một user cụ thể
    @GetMapping("/history/{userId}")
    public ResponseEntity<List<HistoryLog>> getHistory(@PathVariable String userId) {
        return ResponseEntity.ok(streamingService.getUserHistory(userId));
    }

    // API lấy Top Trending cho Front-end hiển thị
    @GetMapping("/trending")
    public ResponseEntity<Set<ZSetOperations.TypedTuple<String>>> getTrending(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(streamingService.getTopTrending(limit));
    }
}
