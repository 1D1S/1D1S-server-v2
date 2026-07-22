# Apple Sign In + FCM 서버 설정

값만 채우면 동작하도록 전부 env 플레이스홀더로 분리되어 있다. 하드코딩 시크릿 없음.
env 미설정 시 해당 기능만 비활성화되고 부팅은 정상 진행된다.

---

## 1. Apple Sign In

### 엔드포인트 (웹/앱이 맞춰야 하는 최종 시그니처)

**앱(네이티브)** — 기존 소셜 로그인과 동일하게 native 교환 흐름을 사용한다. 별도 엔드포인트 없음.

```
POST /auth/native/login/exchange
{
  "provider": "APPLE",
  "credentialType": "ID_TOKEN",
  "credential": "<Apple identityToken>",
  "nonce": "<선택, 로그인 요청에 넣은 nonce>",
  "deviceId": "<필수>"
}
→ 200
{
  "message": "소셜 로그인 연결 성공했습니다.",
  "data": {
    "native": {
      "accessToken": "...", "refreshToken": "...",
      "accessTokenExpiresIn": 3600, "refreshTokenExpiresIn": 1209600
    },
    "webBootstrapCode": "...", "webBootstrapExpiresIn": 60,
    "profileComplete": false
  }
}
```

**웹(브라우저)** — Apple JS SDK로 받은 identityToken을 POST하면 WEBVIEW 세션 쿠키를 심는다 (구글 웹 흐름과 동일 응답 형식).

```
POST /auth/apple/login
{
  "identityToken": "<필수, Apple JS SDK identityToken>",
  "authorizationCode": "<선택>",
  "name": "<선택, 최초 로그인 시만 옴>",
  "email": "<선택, 최초 로그인 시만 옴>",
  "nativeCodeChallenge": "<선택, Flutter WebView 브릿지용 43자 base64url>"
}
→ 200 (Set-Cookie: accessToken, refreshToken)
{
  "message": "소셜 로그인 연결 성공했습니다.",
  "data": { "profileComplete": false }   // nativeCodeChallenge를 준 경우 nativeLoginCode/…ExpiresInSeconds가 대신 옴
}
```

### 검증/식별 규칙
- `identityToken`을 Apple JWKS(`https://appleid.apple.com/auth/keys`)로 서명 검증.
- `iss = https://appleid.apple.com`, `aud ∈ APPLE_AUDIENCE`, `exp` 확인. nonce가 오면 일치 확인.
- 회원 식별 = `APPLE + sub`. 이메일은 최초 동의 시 토큰에 포함되며 표시/초기 프로필용.
- 이름은 Apple이 토큰에 담지 않으므로 프로필 완성 단계에서 닉네임으로 수집(기존 소셜과 동일).

### env
| env | 설명 |
|---|---|
| `APPLE_AUDIENCE` | ID token `aud` 허용값. web(Services ID)과 app(bundle id)이 다르면 **콤마로 나열**. 미설정 시 `APPLE_CLIENT_ID` 사용 |
| `APPLE_CLIENT_ID` | Services ID 또는 앱 bundle id (client_secret의 `sub`) |
| `APPLE_TEAM_ID` | Apple Developer Team ID |
| `APPLE_KEY_ID` | .p8 Key ID |
| `APPLE_PRIVATE_KEY` | **.p8 파일 내용 전체** (`-----BEGIN PRIVATE KEY-----` 포함). 경로 아님 |
| `APPLE_REDIRECT_URI` | 웹 OAuth redirect URI |

> `client_secret`(ES256 JWT)은 `AppleClientSecretGenerator`가 위 env로 생성한다.
> 현재는 로그인에 identityToken 검증만 쓰므로 client_secret은 **회원 탈퇴 시 Apple 토큰 폐기(revoke) 훅**에서 사용하도록 준비만 되어 있다(미연결). `authorizationCode`는 그 훅을 위해 받아만 둔다.

---

## 2. FCM 디바이스 토큰 / 푸시

### 엔드포인트 (Bearer 필요)

```
POST /push/device-tokens
{ "token": "<FCM 등록 토큰>", "platform": "IOS" | "ANDROID", "deviceId": "<기기 고유 id, unique>" }
→ 200 { "message": "디바이스 토큰 등록 성공했습니다." }
```
deviceId 기준 upsert (같은 기기면 토큰/소유자 갱신).

```
DELETE /push/device-tokens
{ "deviceId": "<기기 고유 id>" }
→ 200 { "message": "디바이스 토큰 삭제 성공했습니다." }
```
로그아웃 시 호출. 해당 기기 토큰 비활성화(소유자 불일치는 무시).

### 발송 서비스
`PushSender.sendToMember(memberId, PushMessage.of(title, body, path))`
- 회원의 활성 토큰 조회 → FCM Admin SDK(HTTP v1)로 멀티캐스트 발송.
- `UNREGISTERED`/`INVALID_ARGUMENT` 토큰은 자동 비활성화.
- Firebase 미설정 시 no-op(로그만).

> **실제 트리거(채팅/공지)는 아직 미연결.** 트리거 지점에서 `pushSender.sendToMember(...)`만 호출하면 된다.

### env
| env | 설명 |
|---|---|
| `FIREBASE_SERVICE_ACCOUNT_JSON` | 서비스계정 JSON의 **내용 전체** 또는 **파일 경로**. 미설정 시 FCM 발송만 비활성 |

---

## 3. 마이그레이션
- `V45__add_device_token.sql` — `device_token` 테이블(deviceId unique, member FK, platform, is_active).
- Apple은 `member.social_id`(`sub`) + 기존 `signup_route=APPLE`로 식별하므로 **member 신규 컬럼 없음**.

## 4. 로컬 빌드
```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./gradlew build            # 전체 테스트는 MySQL/Postgres 연결 필요
./gradlew compileJava test --tests "*AppleClientSecretGeneratorTest"   # DB 불필요
```
