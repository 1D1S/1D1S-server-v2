#!/usr/bin/env bash
# 기존 S3 오브젝트에 Cache-Control 메타데이터를 일괄 재설정한다 (self-copy).
#   - 오브젝트별 원본 Content-Type을 head-object로 읽어 그대로 유지 (png/jpeg 혼재 대응)
#   - metadata-directive REPLACE 이므로 명시하지 않은 메타데이터는 사라질 수 있음
#     → 여기서는 Content-Type + Cache-Control만 유지/설정 (다른 커스텀 메타데이터는 미사용 전제)
#
# 사용법:
#   ./scripts/backfill-cache-control.sh odos-bucket-main
#   ./scripts/backfill-cache-control.sh odos-bucket-dev
#
# ponytail: 단순 순차 루프. 오브젝트 수십만 넘어가면 xargs -P 로 병렬화.
set -euo pipefail

BUCKET="${1:?usage: $0 <bucket-name>}"
REGION="ap-northeast-2"
CACHE_CONTROL="public, max-age=31536000, immutable"

echo "Backfilling Cache-Control on s3://${BUCKET} ..."

aws s3api list-objects-v2 --bucket "$BUCKET" --region "$REGION" \
  --query 'Contents[].Key' --output text | tr '\t' '\n' | while read -r KEY; do
  [ -z "$KEY" ] && continue

  CT=$(aws s3api head-object --bucket "$BUCKET" --key "$KEY" --region "$REGION" \
        --query 'ContentType' --output text)
  # head-object 가 ContentType 없으면 "None" 반환 → 안전한 기본값
  [ "$CT" = "None" ] && CT="application/octet-stream"

  aws s3api copy-object --bucket "$BUCKET" --key "$KEY" --region "$REGION" \
    --copy-source "${BUCKET}/${KEY}" \
    --metadata-directive REPLACE \
    --content-type "$CT" \
    --cache-control "$CACHE_CONTROL" \
    --output text --query 'CopyObjectResult.ETag' >/dev/null

  echo "  ✓ ${KEY}  (${CT})"
done

echo "Done. Verify:"
echo "  curl -sI \"https://${BUCKET}.s3.${REGION}.amazonaws.com/<object-key>\" | grep -i cache-control"
