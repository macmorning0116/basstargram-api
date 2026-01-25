# AI-Fishing

> 위치/날씨/사진 정보를 기반으로 낚시 포인트 분석 결과를 제공하는 API 서버입니다.  
> (프론트엔드: React + TypeScript, 인프라: AWS EC2 + Docker + Nginx, CI/CD: GitHub Actions + GHCR)

<!-- TODO: 배너 이미지 / 대표 스크린샷 추가 -->

<img width="210" alt="Image" src="https://github.com/user-attachments/assets/5a099fa4-20b5-49b4-a7fe-69c7498fa2e1" style="margin-right: 12px" />

<img width="210" alt="Image" src="https://github.com/user-attachments/assets/d3660a5c-9ae9-48c0-a8e3-1b3508314ca3" style="margin-right: 12px"/>

<img width="210" alt="Image" src="https://github.com/user-attachments/assets/d2410516-0467-4258-852d-c644c4454065" />

## ✨ 주요 기능 (Features)

- **사진 기반 분석 API**
  - 사용자가 업로드한 사진을 기반으로 낚시 관점의 코칭/가이드 텍스트 제공
- **실시간 날씨 연동**
  - 사용자의 위치 기반으로 현재 날씨 정보를 함께 반영하여 추천/주의사항 제공
- **현재 위치 지도 정보 제공**
  - 편의성을 위해 네이버 지도 API와 연동하여 지도를 보여주며, 주소 정보를 표시합니다.
- **직전 분석 결과 조회 기능**
  - 바로 직전에 분석한 결과를 저장하여 다시 확인할 수 있습니다.


<!-- TODO: 실제 제공하는 엔드포인트에 맞게 기능 항목 수정/추가 -->


## 🧱 Tech Stack

### Frontend
- **React**
- **TypeScript**


### Backend
- **Java 17**
- **Spring Boot 3.4.1**
- **Lombok**

### Infrastructure / DevOps
- **AWS EC2**
- **Docker**
- **Nginx**
- **GitHub Actions**
- **GHCR (GitHub Container Registry)**

### Test
- **JUnit 5**
- **Spring Boot Test**
- **MockWebServer (okhttp3)**: 외부 API 호출 테스트


## 🏗️ Architecture

<!-- TODO: 아키텍처 다이어그램 이미지 추가 -->

<img width="600" alt="Image" src="https://github.com/user-attachments/assets/aa557bd7-0493-4f6e-8299-92253755cab1" />



## 📦 Repository Structure 



```text
ai-fishing-api/
 ├─ gradle/
 ├─ src/
 │  ├─ main/
 │  │  ├─ java/
 │  │  │   ├─ domain/
 │  │  │   │  ├─ analysis/
 │  │  │   │  ├─ map/
 │  │  │   │  └─ weather/
 │  │  │   ├─ global/
 │  │  │   │  ├─ config/
 │  │  │   │  ├─ exception/
 │  │  │   │  ├─ external/
 │  │  │   │  ├─ logging/
 │  │  │   │  └─ response/
 │  │  │   └─ FishingApiApplication.java
 │  │  └─ resources/
 │  └─ test/
 │     └─ java/
 ├─ .github/
 │  └─ workflows/
 │     └─ deploy.yml
 ├─ .gitignore
 ├─ build.gradle
 ├─ Dockerfile
 └─ docker-compose.yml
 
