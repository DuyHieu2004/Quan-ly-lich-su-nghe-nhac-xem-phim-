#  Hệ thống Quản lý Lịch sử Nghe Nhạc & Xem Phim (Real-time Streaming Analytics)

Đồ án này xây dựng một hệ thống theo dõi và phân tích dữ liệu người dùng thời gian thực (như Spotify/Netflix), tập trung vào khả năng xử lý **Heavy Write** (ghi khối lượng dữ liệu khổng lồ) bằng **Cassandra** và **Real-time Analytics** (tính toán tức thời) bằng **Redis**.

##  Công nghệ sử dụng
- **Backend:** Java Spring Boot
- **Frontend:** Angular 17
- **Database:** Apache Cassandra (Lưu trữ Time-series Log) & Redis (Tính toán Top Trending)
- **Infrastructure:** Docker

---

## 🛠 Hướng dẫn Cài đặt & Chạy dự án (Chi tiết từ A-Z)

### Bước 1: Khởi động Database (Docker)
Mở Terminal và chạy 2 lệnh sau để tải và khởi tạo Redis cùng Cassandra:
```bash
docker run --name my-redis -p 6379:6379 -d redis
docker run --name my-cassandra -p 9042:9042 -d cassandra
```
*(Lưu ý: Nếu những ngày sau bạn mở máy lên lại, bạn chỉ cần gõ lệnh: `docker start my-redis my-cassandra` để bật chúng lên chứ không cần chạy lại 2 lệnh run ở trên).*

### Bước 2: Khởi tạo Cấu trúc Database cho Cassandra
Vì Spring Boot không tự động tạo Keyspace (Không gian lưu trữ), bạn cần mở Terminal và chạy lệnh ép Docker gọi công cụ `cqlsh` để tạo bảng:

```bash
docker exec -it my-cassandra cqlsh -e "CREATE KEYSPACE IF NOT EXISTS streaming_app WITH replication = {'class':'SimpleStrategy', 'replication_factor':1}; USE streaming_app; CREATE TABLE IF NOT EXISTS history_logs (user_id text, event_time timestamp, media_id text, media_type text, stopped_at_second int, PRIMARY KEY ((user_id), event_time)) WITH CLUSTERING ORDER BY (event_time DESC);"
```
*(Lệnh trên thực hiện 2 việc: Tạo Database `streaming_app` và Tạo Bảng `history_logs` chuẩn Time-series cho bài toán lịch sử).*

### Bước 3: Khởi chạy Backend (Java Spring Boot)
Mở Terminal tại thư mục gốc của dự án (nơi có chứa file `pom.xml`) và chạy lệnh:
```bash
mvn spring-boot:run
```
 **VŨ KHÍ BÍ MẬT KHI DEMO:** Ngay khi Backend chạy thành công, một **Script chạy ngầm (Data Simulator)** sẽ tự động kích hoạt. Nó liên tục mô phỏng hành vi người dùng, bắn hàng trăm bản ghi lịch sử nghe nhạc vào Database mỗi giây để chứng minh hệ thống của bạn chịu tải "Heavy Write" cực kỳ mượt mà đúng như yêu cầu của Giảng viên.

### Bước 4: Khởi chạy Giao diện Frontend (Angular)
Mở thêm **một cửa sổ Terminal MỚI**, di chuyển vào thư mục `frontend` và khởi chạy giao diện Web:
```bash
cd frontend
npm start
```

### Bước 5: Xem kết quả rực rỡ
Sau khi Bước 4 biên dịch xong (báo màu xanh lá cây), bạn hãy mở trình duyệt Web (Chrome, Cốc Cốc, Edge...) và truy cập vào địa chỉ:
**http://localhost:4200**

Bạn sẽ thấy một bảng xếp hạng (Dashboard) giao diện Dark Mode hiển thị **Top Trending** siêu xịn. Số lượt view sẽ nhảy lên liên tục theo từng giây nhờ dữ liệu khổng lồ đang được Script ngầm đẩy vào hệ thống!

---

## Hướng dẫn xem trực tiếp dữ liệu thô (Dành cho lúc đi Báo cáo)

Phòng trường hợp giáo viên bắt mở Database lên để kiểm chứng, bạn chỉ cần copy các lệnh sau dán vào Terminal:

**1. Xem Cassandra đã ghi được bao nhiêu ngàn dòng?**
```bash
docker exec -it my-cassandra cqlsh -e "SELECT count(*) FROM streaming_app.history_logs;"
```
**2. Xem thử 100 dòng dữ liệu gốc trong Cassandra:**
```bash
docker exec -it my-cassandra cqlsh -e "SELECT * FROM streaming_app.history_logs LIMIT 100;"
```
**3. Xem Redis có đang hoạt động không?**
```bash
docker exec -it my-redis redis-cli dbsize
```

---

## 6 Câu truy vấn trên Cassandra

Vì Cassandra là cơ sở dữ liệu NoSQL dạng Column-Family nên cách truy vấn rất khắt khe (không hỗ trợ `JOIN`, `GROUP BY` phức tạp...). Dưới đây là 6 câu truy vấn thể hiện sự am hiểu về kiến trúc của Cassandra:

**1. Truy vấn Range Scan (Tìm kiếm trong khoảng thời gian)**
*   **Yêu cầu:** Lấy lịch sử của một người dùng từ ngày A đến ngày B.
*   **Lý do phức tạp:** Trong Cassandra, để dùng toán tử `>=` và `<=`, ta **bắt buộc** phải chỉ định chính xác Partition Key (`user_id`), và cột điều kiện phải là Clustering Column (`event_time`).
*   **CQL:**
    ```sql
    SELECT * FROM streaming_app.history_logs WHERE user_id = 'user_1' AND event_time >= '2023-10-01 00:00:00' AND event_time <= '2023-10-31 23:59:59';
    ```

**2. Truy vấn Aggregation (Thống kê tổng số lượng trên phân tán)**
*   **Yêu cầu:** Đếm tổng số lần một người dùng cụ thể đã nghe nhạc/xem phim.
*   **Lý do phức tạp:** Thao tác gom nhóm `COUNT(*)` đòi hỏi quét qua nhiều dòng dữ liệu trong cùng một Partition.
*   **CQL:**
    ```sql
    SELECT COUNT(*) FROM streaming_app.history_logs WHERE user_id = 'user_1';
    ```

**3. Truy vấn Limit & Ordering (Top N hoạt động gần nhất)**
*   **Yêu cầu:** Lấy ra 5 hoạt động gần đây nhất của một người dùng.
*   **Lý do phức tạp:** Lợi dụng việc dữ liệu lưu trong Cassandra đã được sắp xếp sẵn trên ổ cứng theo chiều giảm dần của `event_time` (nhờ cài đặt `CLUSTERING ORDER BY (event_time DESC)`). Câu lệnh này giúp bóc tách dữ liệu cực nhanh kèm phân trang (`LIMIT`).
*   **CQL:**
    ```sql
    SELECT * FROM streaming_app.history_logs WHERE user_id = 'user_1' ORDER BY event_time DESC LIMIT 5;
    ```

**4. Truy vấn Full Partition Scan có điều kiện (ALLOW FILTERING)**
*   **Yêu cầu:** Tìm kiếm các phiên xem phim/nghe nhạc bị bỏ dở giữa chừng (ví dụ `stopped_at_second > 0`) của một user.
*   **Lý do phức tạp:** Cột `stopped_at_second` không nằm trong Primary Key. Bình thường Cassandra sẽ từ chối truy vấn này. Việc thêm từ khóa `ALLOW FILTERING` thể hiện việc lập trình viên hiểu rõ rủi ro đánh đổi hiệu năng để ép Cassandra rà soát (filter) lại dữ liệu sau khi đã lấy ra khỏi ổ cứng.
*   **CQL:**
    ```sql
    SELECT * FROM streaming_app.history_logs WHERE user_id = 'user_1' AND stopped_at_second > 0 ALLOW FILTERING;
    ```

**5. Truy vấn kết hợp Clustering Key và ALLOW FILTERING**
*   **Yêu cầu:** Tìm xem một user đã nghe một bài hát cụ thể (`media_id`) vào thời điểm cuối cùng là khi nào.
*   **Lý do phức tạp:** Là sự kết hợp tổng hòa giữa khóa phân vùng (`user_id`), toán tử lấy 1 dòng giới hạn (`LIMIT 1`) để tiết kiệm băng thông và quét cột thường (`media_id`).
*   **CQL:**
    ```sql
    SELECT * FROM streaming_app.history_logs WHERE user_id = 'user_1' AND media_id = 'song_555' LIMIT 1 ALLOW FILTERING;
    ```

**6. Truy vấn thông qua Secondary Index (Chỉ mục phụ)**
*   **Yêu cầu:** Lọc ra toàn bộ các bản ghi thuộc loại nội dung là Phim (`VIDEO`).
*   **Lý do phức tạp:** Bình thường Cassandra **không cho phép** tìm kiếm trực tiếp trên cột `media_type` mà không có `user_id`. Câu lệnh này yêu cầu phải tối ưu DB bằng cách tạo Secondary Index (Chỉ mục phụ) trên cột `media_type` trước đó để phục vụ việc tìm kiếm trên quy mô toàn cụm (Cluster).
*   **CQL Tạo Index:**
    ```sql
    CREATE INDEX IF NOT EXISTS idx_media_type ON streaming_app.history_logs(media_type);
    ```
*   **CQL Truy vấn:**
    ```sql
    SELECT * FROM streaming_app.history_logs WHERE media_type = 'VIDEO' LIMIT 50;
    ```