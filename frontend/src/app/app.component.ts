import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HttpClient } from '@angular/common/http';

interface TrendingItem {
  value: string;
  score: number;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.css'
})
export class AppComponent implements OnInit, OnDestroy {
  trendingList: TrendingItem[] = [];
  intervalId: any;
  maxScore: number = 1;

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.fetchTrending();
    // Tự động gọi API mỗi 1 giây để cập nhật dữ liệu Real-time
    this.intervalId = setInterval(() => {
      this.fetchTrending();
    }, 1000);
  }

  ngOnDestroy() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }

  fetchTrending() {
    this.http.get<TrendingItem[]>('http://localhost:8080/api/streaming/trending?limit=10')
      .subscribe({
        next: (data) => {
          this.trendingList = data;
          if (data && data.length > 0) {
            this.maxScore = data[0].score;
          }
        },
        error: (err) => console.error('Lỗi khi tải dữ liệu Top Trending:', err)
      });
  }

  getBarWidth(score: number): string {
    if (this.maxScore === 0) return '0%';
    const percentage = (score / this.maxScore) * 100;
    return `${percentage}%`;
  }
}
