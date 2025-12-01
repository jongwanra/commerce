import http from 'k6/http';
import {check, sleep} from 'k6';

// 일간 상품 랭킹 목록 조회 부하 테스트 시나리오
export const options = {
    scenarios: {
        read_daily_top5_product_rankings: {
            executor: 'ramping-arrival-rate',  // 요청 비율을 점진적으로 증가
            startRate: 0,  // 시작 시 요청 비율
            timeUnit: '1s',
            preAllocatedVUs: 1,  // 초기 할당된 VUs
            maxVUs: 1700,  // 최대 VUs 수
            stages: [
                {target: 100 * 1.70, duration: '5s'}, // 170 RPS
                {target: 150 * 1.70, duration: '5s'}, // 255 RPS

                {target: 200 * 1.70, duration: '5s'}, // 340 RPS
                {target: 250 * 1.70, duration: '5s'}, // 425 RPS

                {target: 300 * 1.70, duration: '5s'}, // ...
                {target: 350 * 1.70, duration: '5s'},

                {target: 400 * 1.70, duration: '5s'},
                {target: 450 * 1.70, duration: '5s'},

                {target: 500 * 1.70, duration: '5s'},
                {target: 550 * 1.70, duration: '5s'},

                {target: 600 * 1.70, duration: '5s'},
                {target: 650 * 1.70, duration: '5s'},

                {target: 700 * 1.70, duration: '5s'},
                {target: 750 * 1.70, duration: '5s'},

                {target: 800 * 1.70, duration: '5s'},
                {target: 850 * 1.70, duration: '5s'},

                {target: 900 * 1.70, duration: '5s'},
                {target: 950 * 1.70, duration: '5s'},

                {target: 1000 * 1.70, duration: '5s'}, // 1,700 RPS
            ],
        },
    },
    thresholds: {
        http_req_duration: ['p(95)<2000'],  // 응답 시간 95%가 2000ms 이하
        http_req_failed: ['rate<0.01'],    // 실패율 1% 미만
    },
};

/**
 * [실행 명령어]
 * K6_WEB_DASHBOARD=true k6 run k6/daily_top5_product_ranking_test.js
 *
 * [Dashboard 확인]
 * http://localhost:5665
 */
export default function () {
    const url = 'http://localhost:8080/api/v1/products/top-selling/daily';
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const res = http.get(url, params);

    // 응답 상태 코드 체크
    check(res, {
        'status is 200': (r) => r.status === 200,
    });

    sleep(Math.random() * 2 + 1);
}


