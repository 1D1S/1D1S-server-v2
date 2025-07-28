# 🧭 1D1S-server-v2

기존에 GraphQL 기반 서버(1D1S-server-v1)을 REST API 기반으로 리팩토링하는 프로젝트입니다.  

(Last Modified at 2025-07-29)

---

### 📌 프로젝트 개요

- 기존의 GraphQL 코드를 REST API로 리팩토링
- 실용적인 협업 규칙 및 컨벤션 강화
- 배포 체계 구축


### 🚩 주요 목표

- GraphQL → REST API 리팩토링
- 컨벤션 기반 코드 리뷰
- 역할 분담을 통한 책임 개발
- 명확한 커뮤니케이션 체계 구축


### Server Architecture
추후 업로드 

---

## 🔀 Git 컨벤션

기본적으로 issue 형성 및 issue 번호에 해당하는 branch 생성 후 작업합니다. 
commit(해당 작업) -> 로컬에서 pull 후 충돌 해결 -> pull request 흐름을 준수합니다. 

### ✅ commit message

- 형식 : `타입: 한글 메시지`, 메시지 시작 **대문자**로 고정
- 커밋 메시지에서 타입은 영어, 내용은 한글
- 개발 흐름을 명시할 수 있도록, v1에 비해 커밋 주기를 줄인다.

ex: `Feat: Challenge 기능 구현`

| 타입       | 설명                  |
|----------|---------------------|
| Feat     | 새로운 기능 추가           |
| Fix      | 버그 수정               |
| Docs     | 문서 수정               |
| Style    | 코드 포맷팅 (공백, 세미콜론 등) |
| Refactor | 리팩토링 (기능 변화 없음)     |
| Test     | 테스트 코드 추가/수정        |
| Chore    | 패키지 매니저 설정 등 기타 수정  |
| Merge    | 브랜치 머지 및 관리         |
| Build    | 빌드 파일 수정            |
| Rename   | 파일 혹 폴더명 수정한 경우     |



### ✅ branch

- 형식 : `type/이슈번호-도메인[-세부작업]`
- 필요시 띄어쓰기는 `-`로 연결
- 최대한 명사 위주로 사용, 구체화가 필요할 경우 동사+명사 활용

ex: `feat/22-diary-comment`, `fix/57-diary-filter`

 

### ✅ branch strategy

| 브랜치 | 설명                |
|-------|-------------------|
| `main` | 운영 서버             |
| `dev` | 테스트 서버            |
| `test` | 작업 통합 (현재 default) |



---


