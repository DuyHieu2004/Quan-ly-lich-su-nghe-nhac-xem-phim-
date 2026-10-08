package com.example.streaming_backend.model;

import org.springframework.data.cassandra.core.cql.Ordering;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

import java.time.Instant;

@Table("history_logs")
public class HistoryLog {
    
    @PrimaryKeyColumn(name = "user_id", type = PrimaryKeyType.PARTITIONED, ordinal = 0)
    private String userId;
    
    @PrimaryKeyColumn(name = "event_time", type = PrimaryKeyType.CLUSTERED, ordering = Ordering.DESCENDING, ordinal = 1)
    private Instant eventTime;

    @Column("media_id")
    private String mediaId;
    
    @Column("media_type")
    private String mediaType; // "AUDIO" hoặc "VIDEO"
    
    @Column("stopped_at_second")
    private Integer stoppedAtSecond;

    public HistoryLog() {}

    public HistoryLog(String userId, Instant eventTime, String mediaId, String mediaType, Integer stoppedAtSecond) {
        this.userId = userId;
        this.eventTime = eventTime;
        this.mediaId = mediaId;
        this.mediaType = mediaType;
        this.stoppedAtSecond = stoppedAtSecond;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public Instant getEventTime() { return eventTime; }
    public void setEventTime(Instant eventTime) { this.eventTime = eventTime; }
    
    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }
    
    public String getMediaType() { return mediaType; }
    public void setMediaType(String mediaType) { this.mediaType = mediaType; }
    
    public Integer getStoppedAtSecond() { return stoppedAtSecond; }
    public void setStoppedAtSecond(Integer stoppedAtSecond) { this.stoppedAtSecond = stoppedAtSecond; }
}
