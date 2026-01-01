#!/bin/bash
# scripts/verify-coupon-integrity.sh

echo "Checking coupon issue integrity..."

REDIS_COUNT=$(docker exec redis redis-cli SCARD "coupon:issue:1")
DB_COUNT=$(docker exec mysql mysql -u application -papplication -N -e \
  "SELECT COUNT(*) FROM hhplus.user_coupon WHERE coupon_id = 1")
COUPON_STOCK=$(docker exec mysql mysql -u application -papplication -N -e \
  "SELECT stock FROM hhplus.coupon WHERE id = 1")

echo "Redis issued: $REDIS_COUNT"
echo "DB issued: $DB_COUNT"
echo "Remaining stock: $COUPON_STOCK"

if [ "$REDIS_COUNT" != "1000" ]; then
    echo "ERROR: Redis count is not 1000 (actual: $REDIS_COUNT)"
    exit 1
fi

if [ "$DB_COUNT" != "1000" ]; then
    echo "ERROR: DB count is not 1000 (actual: $DB_COUNT)"
    exit 1
fi

if [ "$REDIS_COUNT" != "$DB_COUNT" ]; then
    echo "ERROR: Redis and DB don't match (Redis: $REDIS_COUNT, DB: $DB_COUNT)"
    exit 1
fi

if [ "$COUPON_STOCK" != "0" ]; then
    echo "ERROR: Stock is not 0 (actual: $COUPON_STOCK)"
    exit 1
fi

echo "All integrity checks passed"
