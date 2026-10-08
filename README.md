# 🎵 Hệ thống Quản lý Lịch sử Nghe Nhạc & Xem Phim (Real-time Streaming Analytics)

Đồ án này xây dựng một hệ thống theo dõi và phân tích dữ liệu người dùng thời gian thực (như Spotify/Netflix), tập trung vào khả năng xử lý **Heavy Write** (ghi khối lượng dữ liệu khổng lồ) bằng **Cassandra** và **Real-time Analytics** (tính toán tức thời) bằng **Redis**.

## 🚀 Công nghệ sử dụng
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
🔥 **VŨ KHÍ BÍ MẬT KHI DEMO:** Ngay khi Backend chạy thành công, một **Script chạy ngầm (Data Simulator)** sẽ tự động kích hoạt. Nó liên tục mô phỏng hành vi người dùng, bắn hàng trăm bản ghi lịch sử nghe nhạc vào Database mỗi giây để chứng minh hệ thống của bạn chịu tải "Heavy Write" cực kỳ mượt mà đúng như yêu cầu của Giảng viên.

### Bước 4: Khởi chạy Giao diện Frontend (Angular)
Mở thêm **một cửa sổ Terminal MỚI**, di chuyển vào thư mục `frontend` và khởi chạy giao diện Web:
```bash
cd frontend
npm start
```

### Bước 5: Xem kết quả rực rỡ
Sau khi Bước 4 biên dịch xong (báo màu xanh lá cây), bạn hãy mở trình duyệt Web (Chrome, Cốc Cốc, Edge...) và truy cập vào địa chỉ:
👉 **http://localhost:4200**

Bạn sẽ thấy một bảng xếp hạng (Dashboard) giao diện Dark Mode hiển thị **Top Trending** siêu xịn. Số lượt view sẽ nhảy lên liên tục theo từng giây nhờ dữ liệu khổng lồ đang được Script ngầm đẩy vào hệ thống!

---

## 🔍 Hướng dẫn xem trực tiếp dữ liệu thô (Dành cho lúc đi Báo cáo)

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