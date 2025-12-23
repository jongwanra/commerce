import http from 'k6/http';
import {check} from 'k6';


export const options = {
    scenarios: {
        coupon_spike: {
            executor: 'per-vu-iterations', // Virtual User별로 정해진 횟수만큼 실행합니다.
            vus: 2000, // 총 2,000명 (User Id 1 ~ 2,000)
            iterations: 1, // 각자 1번씩만 수행합니다.
            maxDuration: '1m' // 최대 1분 동안 수행합니다.
        }
    },
    thresholds: {
        http_req_duration: ['p(99)<2000'], // p99 요청이 2초 이내
        http_req_failed: ['rate<0.51'], // 에러율 51% 미만
    }
}
/**
 * [실행 명령어]
 * K6_WEB_DASHBOARD=true k6 run k6/issue_coupon_spike_test.js
 *
 * [Dashboard 확인]
 * http://localhost:5665
 */
export default function () {
    const userId = __VU;
    const couponId = 1;
    const url = `http://localhost:8080/api/v2/me/coupons/${couponId}/issue`;
    const params = {
        headers: {
            'Content-Type': 'application/json',
            'Commerce-User-Id': userId
        },
        timeout: '30s'
    };


    const res = http.post(url, null, params);

    // 응답 상태 코드 체크
    check(res, {
        'status is 201 or 400': (r) => [201, 400].includes(r.status),
        'no server error 500': (r) => r.status !== 500
    });

    // 디버깅용: 200이나 400이 아니면 로그 출력
    if (res.status !== 201 && res.status !== 400) {
        console.log(`Unexpected Status: ${res.status} body: ${res.body}`);
    }


}
