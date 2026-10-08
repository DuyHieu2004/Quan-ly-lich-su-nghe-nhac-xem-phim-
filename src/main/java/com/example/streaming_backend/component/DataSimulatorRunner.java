package com.example.streaming_backend.component;

import com.example.streaming_backend.service.StreamingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

@Component
public class DataSimulatorRunner implements CommandLineRunner {

    @Autowired
    private StreamingService streamingService;

    private final AtomicBoolean isSimulating = new AtomicBoolean(true);

    @Override
    public void run(String... args) throws Exception {
        System.out.println("==========================================================");
        System.out.println(" Start process Data Simulator...");
        System.out.println(" Target: Push data into Cassandra and Redis to simulate Heavy Write.");
        System.out.println("==========================================================");
        
        new Thread(() -> {
            Random random = new Random();
            long count = 0;
            while (isSimulating.get()) {
                // Giả lập một người dùng đang nghe nhạc/xem phim
                String randomUser = "User_" + (random.nextInt(1000) + 1); // 1000 users
                String randomMedia = "Media_" + (random.nextInt(100) + 1); // 100 bài hát
                String type = random.nextBoolean() ? "AUDIO" : "VIDEO";
                int stopSec = random.nextInt(300);

                // Ghi vào hệ thống (Vừa ném vào Cassandra, vừa cập nhật Redis)
                streamingService.logListenEvent(randomUser, randomMedia, type, stopSec);
                
                count++;
                if (count % 50 == 0) {
                    System.out.println("🔥 Inserted " + count + " logs into Cassandra & Redis.");
                }

                // Cứ 10ms bắn 1 bản ghi = 100 bản ghi / giây (đáp ứng đúng chữ "hàng chục dòng mỗi giây")
                try {
                    Thread.sleep(10); 
                } catch (InterruptedException e) {
                    System.err.println("Tiến trình giả lập bị gián đoạn!");
                    break;
                }
            }
        }).start();
    }
}
