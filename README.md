## Writing Board Backend
자바 한줄 없는 초기세팅

### Tech Stack, dependencies
build.gradle에 있음

## 로컬 환경 개발 세팅
1. `.env.example` 파일을 복사해서 `.env` 파일을 만들고 값을 채워주세요
2. .env파일은 .gitignore에 포함되어 있습니다. (아마?)
3.  docker-compose up -d 해서 MySQL 실행(이 단계에서는 인텔리제이 환경변수 설정 없어도 됩니당
4.  얼티미트 쓸 경우 인텔리제이에서 연결 확인 가능(선택사항)
5.  아직 개발 시작 안 했지만 세팅 다 하면 `http://localhost:8080/oauth2/authorization/google
`로 접근시 Google 로그인 화면 정상적으로 노출됩니다.
