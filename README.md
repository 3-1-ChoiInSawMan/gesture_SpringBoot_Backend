<div align="center">

<img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/gesture/gesture_icon.png" width="80">

# 제스처 (Gesture)

### 영상통화 기반 실시간 수어 번역 플랫폼

<img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/gesture/%EC%A0%9C%EC%8A%A4%EC%B2%98_%EB%AA%A9%EC%97%85.png" width="800">

</div>

<br>


## 프로젝트 소개

영상통화 환경에서 수어를 실시간 텍스트 자막으로 변환해 **통역사 없이도 청각장애인과 일반인 간의 자연스러운 소통을 가능하게 하는 플랫폼**입니다.

- **BFF 패턴 아키텍처**에서 **Core 서버**를 전담하며 비즈니스 로직 설계 및 데이터 처리 전반 경험
- **Redis Stream 비동기 파이프라인** 도입으로 서버 장애 상황에서도 **회의록 데이터 유실 없이 보존**

<br>

---

## 실사용자 검증 · 한국농아인협회 (대구광역시)

<div align="center">
  <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/gesture/%ED%95%9C%EA%B5%AD%EB%86%8D%EC%95%84%EC%9D%B8%ED%98%91%ED%9A%8C_%EB%8C%80%EA%B5%AC.jpeg" width="250">
</div>

- 한국농아인협회 (대구광역시) 테스트 협의 완료
- 2026년 6월 4일 협회 방문 테스트 진행 예정

<br>

---

## 📊 기획 발표 자료

> 🎨 [Canva 기획 발표 보러가기](https://canva.link/pskhsrqexkdn9w7)

<br>

---

## 팀 구성

> ⛓️ Team - 최인소맨 (ChoiInSawMan)  
> 구성 : 프론트엔드 1 · 백엔드 3 · AI 1

<div align="center">

| **[@yeeeengyu](https://github.com/yeeeengyu)** | **[@hwnsng](https://github.com/hwnsng)** | **[@yoonjeonggg](https://github.com/yoonjeonggg)** | **[@acorn497](https://github.com/acorn497)** | **[@swimim](https://github.com/swimim)** |
| :------: | :------: | :------: | :------: | :------: |
| 팀장 | 팀원 | 팀원 | 팀원 | 팀원 |
| AI | 프론트엔드 | 백엔드 (Core 서버) | 백엔드 | 백엔드 |

</div>

<br>

---

## 1. 개발 기간 및 작업 관리

### 개발 기간

- 전체 개발 기간 : 2026.03.10 ~ 진행 중 🔥

<br>

### 작업 관리

- 매주 수요일 (주)에이포랩과 **정기 영상통화 회의**로 진행 상황 공유 및 방향 조율
- **GitHub Actions · Discord 연동**으로 PR 발생 시 자동 알림 → 팀 전체 진행 상황 실시간 공유
- BFF 패턴 아키텍처 도입 · BFF(NestJS) · Core(Spring Boot) 역할 분리 및 API 명세서 · **개발 규칙 사전 문서화**

> 📎 [BFF 패턴을 선택한 이유와 설계 과정](https://www.notion.so/BFF-36e0aa80477f80c7b4b4f8b99f5407bd?pvs=21)

<br>

---

## 2. 개발 환경

- **Backend (Core)** : Java 21, Spring Boot 3.5, MySQL 8.0, Redis
- **Frontend** : NestJS (BFF), React
- **AI** : Python
- **배포** : AWS EC2, Docker, GitHub Actions (CI/CD)
- **버전 및 이슈관리** : Github, Github Issues
- **협업 툴** : Notion, Github, Discord
- **산학 협력** : (주)에이포랩

<br>

---

## 3. 기술 스택

<div align="center">
  <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/gesture/gesture_%ED%94%84%EB%A0%88%EC%9E%84%EC%9B%8C%ED%81%AC.png" alt="프레임워크" width="800">
</div>

<br>

| 구분 | 기술 |
|------|------|
| Language | Java 21 |
| Framework | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/springBoot_icon.png" width="16"> Spring Boot 3.5 |
| Database | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/mysql_icon.png" width="16"> MySQL 8.0 |
| Cache / Stream | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/redis_icon.png" width="16"> Redis, Redis Stream |
| Security | Spring Security, JWT (RS256 비대칭 키) |
| Storage | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/aws_icon.png" width="16"> AWS S3 |
| Mail | Brevo |
| Infra | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/AWS_EC2_icon.png" width="16"> AWS EC2, Docker |
| CI/CD | GitHub Actions |
| API Docs | Swagger (SpringDoc OpenAPI 3) |
| Collaboration | <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/stacks/github_icon.png" width="16"> Github, Notion, Discord |

<br>

### 기술 선정 이유

#### Redis Stream
- BFF 서버가 수집한 회의록 데이터를 비동기로 처리하기 위해 도입했습니다.
- 수동 ACK 방식으로 **RDB 적재 성공 시점에만 ACK 발송**하여 데이터 유실을 방지했습니다.
- 장애 발생 시 메시지가 **PEL 상태로 보존**되어 안전하게 복구 가능합니다.

#### RS256 비대칭 키 JWT
- BFF · Core 서버 분리 구조에서 HS256 대칭 키 방식은 Secret Key 공유로 인한 보안 문제가 발생합니다.
- **Private Key로 서명, Public Key로 검증**하는 RS256 방식으로 전환해 보안을 강화했습니다.

> 📎 [기술 스택을 선정한 이유](https://www.notion.so/36d0aa80477f80f4ab19c2cff834487e?pvs=21)

<br>

---

## 4. ☁️ 서비스 아키텍처

<div align="center">
  <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/gesture/%EC%A0%9C%EC%8A%A4%EC%B2%98%20%EC%84%9C%EB%B9%84%EC%8A%A4%20%EC%95%84%ED%82%A4%ED%85%8D%EC%B2%98.png" alt="서비스 아키텍처" width="800">
</div>

<br>

---

## 5. 🗄 ERD

<div align="center">
  <img src="https://raw.githubusercontent.com/yoonjeonggg/readme-assets/main/gesture/%EC%A0%9C%EC%8A%A4%EC%B2%98_ERD.png" alt="ERD" width="800">
</div>

<br>

---

## 6. 📁 프로젝트 구조

```
src/main/java/.../
├── controller/       # REST API 엔드포인트 (/internal)
├── service/          # 비즈니스 로직
├── repository/       # 데이터 접근 계층 (JPA)
├── domain/           # JPA 엔티티
│   └── enums/        # 상태 열거형
├── dto/              # 요청/응답 DTO
├── auth/             # JWT (RS256), Spring Security
├── stream/           # Redis Stream 컨슈머
├── global/
│   ├── config/       # Security, Redis, S3 설정
│   └── common/       # ApiResponse, 공통 예외
└── exception/        # CustomException, GlobalExceptionHandler
```

<br>

---

## 7. 역할 분담

### [@yoonjeonggg](https://github.com/yoonjeonggg) · 백엔드 (Core 서버) · 기획

**인증 시스템 구축** `Spring Security` `JWT` `RS256`
- RS256 비대칭 키 기반 JWT 토큰 발급 · 검증 및 Spring Security 연동
- BFF 서버에서 인증 처리 후 Core 서버에서 **공개키로 JWT 검증**하는 구조 설계
- Refresh Token 저장 · 삭제 및 Access Token 재발급 로직 구현
- BCrypt 기반 비밀번호 암호화 및 소프트 삭제 방식의 회원 탈퇴 처리

**통화방 및 실시간 통화 세션 관리** `JPA` `통화 세션`
- 통화방 생성 · 참여 · 나가기 및 **방장 자동 위임** 로직 구현
- 공개 · 비공개 방 설정 및 카테고리 · 제목 기반 방 검색 기능 구현
- 통화 세션 생성 · 종료 및 실시간 참가자 관리 구현

**회의록 자동 생성 시스템 구축** `Redis Stream` `AI 연동` `비동기 처리`
- BFF 서버가 수집한 회의록 데이터를 실시간으로 처리하는 **Redis Stream 전용 컨슈머** 구현
- RDB 적재 트랜잭션 성공 시점에만 ACK 발송하는 **수동 ACK** 방식으로 **데이터 유실 방지**
- 처리 중 예외 발생 시 메시지가 **PEL 상태로 보존**되어 장애 상황에서도 안전하게 복구 가능
- transcript · participants · conclusion 등 JSON 데이터 파싱 및 DB 적재 로직 구현

**이메일 인증 시스템 구현** `Redis` `Brevo` `이메일 인증`
- Redis TTL 기반 6자리 인증 코드 저장 및 만료 처리 (5분)
- Brevo 메일 서비스 연동 및 도메인 설정을 통한 이메일 인증 기능 구현

<br>

---

## 8. ✨ 주요 기능

### 🔐 인증 · 회원
- JWT (RS256) 기반 로그인 및 리프레시 토큰 자동 로그인
- 소셜 로그인 (OAuth 기반 소셜 계정 연동)
- Redis TTL 기반 이메일 인증 (Brevo)
- 회원가입, 프로필 조회 · 수정, 비밀번호 변경, 소프트 삭제 방식 회원 탈퇴

### 📞 통화방 · 통화 세션
- 공개 · 비공개 방 생성, 참여, 나가기
- 방장 자동 위임 로직
- 카테고리 · 제목 기반 방 검색
- 통화 세션 생성 · 종료 및 실시간 참가자 관리

### 📝 회의록 자동 생성
- 통화 세션 기반 회의록 자동 생성
- Redis Stream 비동기 파이프라인으로 데이터 유실 없이 안전하게 처리
- AI 요약 연동 (transcript · participants · conclusion)
- 회의록 조회 · 수정 · 삭제

### 👥 친구
- 친구 요청 · 수락 · 거절 · 삭제
- 친구 목록 조회, 통화방 초대

### 🔔 알림
- 타입별 알림, 수신 설정

### ⚡ 퀵슬롯
- 자주 쓰는 액션 템플릿 관리 (최대 30개, 프리셋 5개)

### 🖼️ 미디어
- AWS S3 파일 업로드 · 조회 (UUID 기반 설계)

### 🔜 추후 개발 예정
- **수어 학습 기능** — 수어 데이터 크롤링 및 수어 인식 AI 연동으로 정확도 체크

<br>

---

## 9. 🚨 성능 개선 및 트러블슈팅

### 🔑 HS256 → RS256 JWT 서명 방식 변경

**문제 원인**
- BFF · Core 서버 분리 구조에서 HS256 대칭 키 방식은 Secret Key 공유로 인한 보안 문제 발생

**해결 과정**
- RS256 비대칭 키 방식으로 변경 → Private Key로 서명, Public Key로 BFF에서 검증하는 구조로 전환
- 환경별 PEM 파일 분리 및 GitHub Secrets 등록으로 보안 관리

**배운 점**
- 멀티 서버 환경에서는 단순 구현이 아닌 **보안 설계를 고려한 인증 구조**가 중요함을 체감

> 📎 [관련 글 보러가기](https://www.notion.so/Gesture-BFF-Core-HS256-RS256-JWT-36c0aa80477f80c2ac12f1f3a933ed8d?pvs=21)

<br>

### 📁 파일 업로드 방식 변경 (UUID 기반 설계 개선)

**문제 원인**
- `entityType`으로 바로 DB에 저장하거나 업로드 후 반환된 URL을 요청값에 직접 넣는 방식이 혼용되어 유지보수성 저하
- multipart 방식도 고려했으나 구조가 복잡해 유지보수성이 떨어진다고 판단

**해결 과정**
- 파일 업로드 시 S3에 저장 후 `uuid` 반환, 이후 API 요청 시 `uuid` 함께 전달하는 방식으로 통일
```
저장 흐름 | 파일 → BFF → Core → S3 업로드 → DB 저장 (UUID 반환)
요청 흐름 | UUID → BFF → Core → DB 조회 → S3 URL 반환
```

**배운 점**
- 단순히 동작하는 구조가 아닌 **유지보수성과 일관성을 고려한 API 설계**가 중요함을 경험

> 📎 [관련 글 보러가기](https://www.notion.so/Gesture-UUID-36c0aa80477f80518d40c9338f60a1bc?pvs=21)

<br>

### 🤝 BFF · Core 서버 역할 경계 혼선

**문제 원인**
- BFF 패턴 첫 도입으로 서버 간 역할 경계 정의가 필요했으나 기준 자체가 없었음
- 기능별 담당 서버를 매번 구두로 결정하다 보니 결정한 내용이 반복적으로 누락되는 비효율 발생

**해결 과정**
- 기능별 담당 서버와 처리 흐름을 명확히 정의한 **개발 규칙 문서** 사전 작성
- JWT 토큰 구조 · 에러 코드 · 공통 요청 DTO 등 팀 전체 공유 규격 함께 문서화 → 반복적인 소통 비용 감소

**배운 점**
- 새로운 아키텍처 패턴 도입 시 **기능 분류와 서버 역할 분리는 다른 영역**임을 직접 체감
- 개발 규칙을 사전에 문서화하면 **반복적인 소통 비용을 줄이고 개발 속도를 높일 수 있음**을 경험

> 📎 [관련 글 보러가기](https://www.notion.so/Gesture-BFF-Core-36c0aa80477f80c7882bd1ba7b3e99db?pvs=21)

<br>

---

## 10. API 엔드포인트

- **Swagger UI:** `http://localhost:8082/internal/swagger-ui/index.html`
- **Base URL:** `/internal`

| 카테고리 | 주요 엔드포인트 |
|----------|----------------|
| Auth | 로그인, 회원가입, 소셜 로그인, 토큰 갱신, 로그아웃 |
| User | 프로필 조회/수정, 비밀번호 변경, 회원 탈퇴, 유저 검색 |
| Room | 방 생성/조회/수정/삭제, 방 참여/퇴장, 방 검색 |
| Call | 통화 참여/퇴장, 참여자 조회 |
| Meeting | 회의록 시작/저장/종료, 목록/상세 조회, 수정/삭제 |
| Friend | 친구 요청/수락/거절/삭제, 친구 목록, 통화방 초대 |
| Chat | 그룹 채팅방, 미디어 첨부 |
| Notification | 알림 조회, 수신 설정 |
| QuickSlot | 퀵슬롯 관리 (최대 30개, 프리셋 5개) |
| Media | S3 파일 업로드/조회 |

<br>

---

## 11. 🚀 Getting Started

```bash
# 설정 파일 생성
# src/main/resources/application-local.properties 직접 생성

spring.datasource.url=jdbc:mysql://localhost:3306/{DB명}
spring.datasource.username={유저명}
spring.datasource.password={비밀번호}

aws.s3.bucket={버킷명}
aws.region={리전}

spring.data.redis.host=localhost
spring.data.redis.port=6379

# JWT 키 생성
mkdir -p src/main/resources/keys
openssl genrsa -out src/main/resources/keys/private_key.pem 2048
openssl rsa -in src/main/resources/keys/private_key.pem -pubout \
  -out src/main/resources/keys/public_key.pem

# 실행
./gradlew bootRun
```

### 인증 방식
모든 API 요청 헤더에 `X-User-Id`를 포함해야 합니다. (API Gateway에서 JWT 검증 후 주입)
```
X-User-Id: {userIdx}
```

<br>

---

## 12. 🔁 CI/CD

`main` 브랜치에 push 시 자동 배포

```
GitHub Actions → Gradle 빌드 → Docker 이미지 빌드/푸시 → EC2 배포
```

필요한 GitHub Secrets:

| Secret | 설명 |
|--------|------|
| `DOCKERHUB_USERNAME` / `DOCKERHUB_TOKEN` | Docker Hub 인증 |
| `EC2_HOST` / `EC2_USERNAME` / `EC2_SSH_KEY` | EC2 접속 정보 |
| `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` | DB 접속 정보 |
| `AWS_S3_BUCKET` / `AWS_REGION` | S3 설정 |
| `JWT_PRIVATE_KEY` / `JWT_PUBLIC_KEY` | RS256 키 |

<br>

---

<div align="center">
  <sub>팀 최인소맨 · (주)에이포랩 산학 프로젝트 · 2026</sub>
</div>
