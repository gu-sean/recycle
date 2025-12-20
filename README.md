♻️ Re-Cycle: 게이미피케이션 기반 분리수거 안내 서비스
분리수거도 게임처럼! 사용자의 참여를 유도하고 올바른 분리배출 습관을 형성하는 Java Swing 기반 플랫폼입니다.

🎯 프로젝트 소개 및 목적
기존의 단순 정보 제공 방식은 사용자의 흥미를 끌기 어렵고 지속적인 참여를 유도하는 데 한계가 있습니다.   
본 프로젝트는 이러한 문제를 해결하기 위해 참여형 학습과 보상 체계를 결합했습니다.

- 참여형 학습 (Quiz): 퀴즈를 통해 지식을 습득하고, '1일 1회 제한' 로직으로 꾸준한 습관 형성을 유도합니다.
- 품목 포인트화: 분리배출한 품목을 포인트로 환산하여 자신의 환경 기여도를 시각적으로 확인합니다.
- 보상 및 경쟁 (Ranking): 실시간 Top 5 랭킹 시스템을 통해 성취감과 경쟁 심리를 자극합니다.
- 자원 순환 체계 (Store): '실천 → 보상 → 사용'으로 이어지는 가상 스토어를 통해 자원 순환의 가치를 경험합니다.

## 🛠 Tech Stack

| 분류 | 기술 |
| :--- | :--- |
| **Language** | <img src="https://img.shields.io/badge/Java-007396?style=flat-square&logo=java&logoColor=white"> |
| **Database** | <img src="https://img.shields.io/badge/MySQL-4479A1?style=flat-square&logo=mysql&logoColor=white"> |
| **Server** | <img src="https://img.shields.io/badge/Naver Cloud Platform-03C75A?style=flat-square&logo=naver&logoColor=white"> |
| **IDE** | <img src="https://img.shields.io/badge/Eclipse-2C2255?style=flat-square&logo=eclipseide&logoColor=white"> |


🚀 핵심 기술 포인트
1. 데이터 무결성을 위한 트랜잭션 관리
   - 포인트 적립과 로그 기록이 동시에 이루어져야 하는 로직에서 JDBC 트랜잭션을 적용했습니다.
   - setAutoCommit(false)와 rollback()을 활용하여 데이터 불일치(포인트는 오르고 로그는 안 남는 등)를 원천 차단했습니다.

2. MVC 패턴 기반의 Layered ArchitectureMode
   - Model: DAO/DTO 패턴을 적용하여 데이터 접근 로직과 객체를 분리했습니다.
   - View: Swing을 활용한 독립적인 UI 컴포넌트 구성.
   - Controller: 사용자 이벤트 처리를 담당하는 컨트롤러 계층 분리.

3. 클라우드 DB 연동
   - 로컬 DB의 한계를 넘어 Naver Cloud Platform을 통해 서버를 호스팅하여, 언제 어디서든 동일한 데이터에 접근할 수 있도록 환경을 구축했습니다.
