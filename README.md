# 데이터베이스 ERD 구조 요약
## 주요 관계 설명

### Member(1) : Device(N)

한 명의 사용자는 여러 대의 디바이스(스마트 워치, 체중계 등)를 가질 수 있습니다.

Device 테이블의 member_id가 외래키(FK) 역할을 합니다.


### Device(1) : HealthRecord(N)

디바이스는 정해진 시간(upload_time)마다 건강 레코드를 업로드합니다.

@UniqueConstraint를 통해 동일한 디바이스가 같은 시간에 중복 데이터를 올리는 것을 방지했습니다.


### HealthRecord(1) : HealthDetail(N)

한 번의 업로드(Record) 안에 여러 개의 상세 활동 내역(Detail, 예: 10분 단위 걸음 수)이 포함되는 구조입니다.

CascadeType.ALL을 통해 레코드가 저장될 때 상세 내역도 함께 저장되도록 설계되었습니다.

HealthSummary (별도 요약 테이블)

조회 성능을 극대화하기 위한 통계용 테이블입니다.

member_id, device_id, summaryDate를 묶어 유니크 제약을 줌으로써 하루에 단 하나의 요약 레코드만 존재하게 설정했습니다.