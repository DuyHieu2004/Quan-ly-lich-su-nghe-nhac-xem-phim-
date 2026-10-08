package com.example.streaming_backend.repository;

import com.example.streaming_backend.model.HistoryLog;
import org.springframework.data.cassandra.repository.CassandraRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryLogRepository extends CassandraRepository<HistoryLog, String> {
    // Tự động generate câu query tìm lịch sử nghe của 1 user cụ thể
    List<HistoryLog> findByUserId(String userId);
}
