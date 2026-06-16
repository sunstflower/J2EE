# 功能性测试脚本说明

## 1. 目标

本文档提供一套可直接在终端执行的 Shell 脚本，用于验证当前系统最小演示闭环的功能性：

- 健康检查
- 医生与药师登录
- 库存分页查询
- 低库存预警查询
- 药物入库
- 医生开药
- 药师越权开具处方药被拒绝

脚本默认面向当前 Docker 演示环境，也可用于本地联调环境。

## 2. 前置条件

执行脚本前请确认：

- 后端接口可访问，默认地址为 `http://localhost:3000/api`
- 本机可使用 `curl`
- 本机可使用 `python3`
- 系统中已存在演示账号：
  - 药师：`1001 / pharm123`
  - 医生：`2001 / doctor123`

如果接口不是通过 `3000` 端口暴露，可在运行时覆盖 `BASE_URL`。

## 3. 使用方式

将以下内容保存为 `test-functional.sh` 后执行：

```bash
#!/usr/bin/env bash

set -euo pipefail

BASE_URL="${BASE_URL:-http://localhost:3000/api}"
TMP_DIR="$(mktemp -d)"
trap 'rm -rf "$TMP_DIR"' EXIT

DOCTOR_ID="${DOCTOR_ID:-2001}"
DOCTOR_PASSWORD="${DOCTOR_PASSWORD:-doctor123}"
PHARMACIST_ID="${PHARMACIST_ID:-1001}"
PHARMACIST_PASSWORD="${PHARMACIST_PASSWORD:-pharm123}"

PATIENT_NAME="${PATIENT_NAME:-功能测试患者}"
INBOUND_QTY="${INBOUND_QTY:-3}"
PRESCRIPTION_QTY="${PRESCRIPTION_QTY:-1}"
TEST_BATCH_NO="${TEST_BATCH_NO:-BATCH-FT-$(date +%Y%m%d%H%M%S)}"
TEST_BIZ_NO="${TEST_BIZ_NO:-BIZ-FT-$(date +%Y%m%d%H%M%S)}"
EXPIRY_DATE="${EXPIRY_DATE:-2030-12-31}"

log() {
  printf '\n[%s] %s\n' "$(date '+%H:%M:%S')" "$1"
}

fail() {
  printf '\n[FAIL] %s\n' "$1" >&2
  exit 1
}

json_get() {
  local file="$1"
  local expr="$2"
  python3 - "$file" "$expr" <<'PY'
import json
import sys

file_path = sys.argv[1]
expr = sys.argv[2]

with open(file_path, "r", encoding="utf-8") as f:
    data = json.load(f)

value = data
for part in expr.split("."):
    if part == "":
        continue
    if "[" in part and part.endswith("]"):
        name, index = part[:-1].split("[", 1)
        if name:
            value = value[name]
        value = value[int(index)]
    else:
        value = value[part]

if isinstance(value, (dict, list)):
    print(json.dumps(value, ensure_ascii=False))
elif value is None:
    print("")
else:
    print(value)
PY
}

assert_code_zero() {
  local file="$1"
  local code
  code="$(json_get "$file" "code")"
  [ "$code" = "0" ] || fail "接口返回失败，code=$code，响应文件=$file"
}

assert_code_equals() {
  local file="$1"
  local expected="$2"
  local code
  code="$(json_get "$file" "code")"
  [ "$code" = "$expected" ] || fail "接口返回码不符合预期，expected=$expected actual=$code"
}

request_json() {
  local method="$1"
  local path="$2"
  local output="$3"
  local auth_header="${4:-}"
  local body="${5:-}"

  local curl_args=(
    -sS
    -X "$method"
    "${BASE_URL}${path}"
    -H 'Accept: application/json'
  )

  if [ -n "$auth_header" ]; then
    curl_args+=(-H "Authorization: Bearer ${auth_header}")
  fi

  if [ -n "$body" ]; then
    curl_args+=(-H 'Content-Type: application/json' --data "$body")
  fi

  curl "${curl_args[@]}" > "$output"
}

login() {
  local user_id="$1"
  local password="$2"
  local output="$3"
  request_json "POST" "/auth/login" "$output" "" "{\"userId\":${user_id},\"password\":\"${password}\"}"
  assert_code_zero "$output"
}

log "1/7 健康检查"
HEALTH_JSON="$TMP_DIR/health.json"
request_json "GET" "/health" "$HEALTH_JSON"
assert_code_zero "$HEALTH_JSON"
[ "$(json_get "$HEALTH_JSON" "data")" = "ok" ] || fail "健康检查未返回 ok"

log "2/7 医生登录"
DOCTOR_LOGIN_JSON="$TMP_DIR/doctor_login.json"
login "$DOCTOR_ID" "$DOCTOR_PASSWORD" "$DOCTOR_LOGIN_JSON"
DOCTOR_TOKEN="$(json_get "$DOCTOR_LOGIN_JSON" "data.token")"
[ -n "$DOCTOR_TOKEN" ] || fail "医生登录成功但 token 为空"

log "3/7 药师登录"
PHARMACIST_LOGIN_JSON="$TMP_DIR/pharmacist_login.json"
login "$PHARMACIST_ID" "$PHARMACIST_PASSWORD" "$PHARMACIST_LOGIN_JSON"
PHARMACIST_TOKEN="$(json_get "$PHARMACIST_LOGIN_JSON" "data.token")"
[ -n "$PHARMACIST_TOKEN" ] || fail "药师登录成功但 token 为空"

log "4/7 查询库存分页并选取测试药品"
INVENTORY_JSON="$TMP_DIR/inventories.json"
request_json "GET" "/inventories?pageNum=1&pageSize=20" "$INVENTORY_JSON" "$DOCTOR_TOKEN"
assert_code_zero "$INVENTORY_JSON"
TOTAL_INVENTORY="$(json_get "$INVENTORY_JSON" "data.total")"
[ "${TOTAL_INVENTORY}" -gt 0 ] || fail "库存列表为空，无法继续功能测试"

TEST_DRUG_ID="$(json_get "$INVENTORY_JSON" "data.records[0].drugId")"
TEST_DRUG_NAME="$(json_get "$INVENTORY_JSON" "data.records[0].drugName")"
BEFORE_QTY="$(json_get "$INVENTORY_JSON" "data.records[0].quantity")"
[ -n "$TEST_DRUG_ID" ] || fail "未能从库存列表中解析 drugId"

log "已选择测试药品：${TEST_DRUG_NAME} (drugId=${TEST_DRUG_ID})，当前库存=${BEFORE_QTY}"

log "5/7 查询低库存预警"
LOW_STOCK_JSON="$TMP_DIR/low_stock.json"
request_json "GET" "/warnings/low-stock?pageNum=1&pageSize=20" "$LOW_STOCK_JSON" "$DOCTOR_TOKEN"
assert_code_zero "$LOW_STOCK_JSON"

log "6/7 药物入库并验证库存增加"
INBOUND_JSON="$TMP_DIR/inbound.json"
request_json "POST" "/inventories/inbound" "$INBOUND_JSON" "$DOCTOR_TOKEN" "$(cat <<EOF
{"drugId":${TEST_DRUG_ID},"batchNo":"${TEST_BATCH_NO}","expiryDate":"${EXPIRY_DATE}","quantity":${INBOUND_QTY},"locationCode":"A-01-TEST","bizNo":"${TEST_BIZ_NO}","remark":"功能测试入库"}
EOF
)"
assert_code_zero "$INBOUND_JSON"

INVENTORY_AFTER_INBOUND_JSON="$TMP_DIR/inventories_after_inbound.json"
request_json "GET" "/inventories?drugId=${TEST_DRUG_ID}&pageNum=1&pageSize=50" "$INVENTORY_AFTER_INBOUND_JSON" "$DOCTOR_TOKEN"
assert_code_zero "$INVENTORY_AFTER_INBOUND_JSON"

AFTER_INBOUND_TOTAL_QTY="$(python3 - "$INVENTORY_AFTER_INBOUND_JSON" <<'PY'
import json
import sys
with open(sys.argv[1], "r", encoding="utf-8") as f:
    payload = json.load(f)
records = payload["data"]["records"]
print(sum(int(item["quantity"]) for item in records))
PY
)"

BEFORE_TOTAL_QTY="$(python3 - <<PY
print(int("${BEFORE_QTY}"))
PY
)"

[ "$AFTER_INBOUND_TOTAL_QTY" -ge $((BEFORE_TOTAL_QTY + INBOUND_QTY)) ] || fail "入库后库存总量未按预期增加"

log "7/7 医生开药成功 + 药师越权失败"
DOCTOR_PRESCRIPTION_JSON="$TMP_DIR/doctor_prescription.json"
request_json "POST" "/prescriptions" "$DOCTOR_PRESCRIPTION_JSON" "$DOCTOR_TOKEN" "$(cat <<EOF
{"patientName":"${PATIENT_NAME}","items":[{"drugId":${TEST_DRUG_ID},"dosage":"1片","frequency":"每日一次","days":1,"quantity":${PRESCRIPTION_QTY}}],"remark":"功能测试医生开药"}
EOF
)"
assert_code_zero "$DOCTOR_PRESCRIPTION_JSON"
PRESCRIPTION_ID="$(json_get "$DOCTOR_PRESCRIPTION_JSON" "data")"
[ -n "$PRESCRIPTION_ID" ] || fail "医生开药成功但未返回处方 ID"

PHARMACIST_PRESCRIPTION_JSON="$TMP_DIR/pharmacist_prescription.json"
request_json "POST" "/prescriptions" "$PHARMACIST_PRESCRIPTION_JSON" "$PHARMACIST_TOKEN" "$(cat <<EOF
{"patientName":"${PATIENT_NAME}-越权","items":[{"drugId":${TEST_DRUG_ID},"dosage":"1片","frequency":"每日一次","days":1,"quantity":1}],"remark":"功能测试药师越权开药"}
EOF
)"

PHARMACIST_CODE="$(json_get "$PHARMACIST_PRESCRIPTION_JSON" "code")"
if [ "$PHARMACIST_CODE" = "0" ]; then
  log "注意：本次选中的测试药品未触发药师越权拦截，通常说明该药不是处方药。"
else
  assert_code_equals "$PHARMACIST_PRESCRIPTION_JSON" "4010"
fi

printf '\n[PASS] 功能性测试完成\n'
printf 'BASE_URL=%s\n' "$BASE_URL"
printf '测试药品=%s (drugId=%s)\n' "$TEST_DRUG_NAME" "$TEST_DRUG_ID"
printf '医生开具处方ID=%s\n' "$PRESCRIPTION_ID"
```

## 4. 推荐执行命令

```bash
chmod +x test-functional.sh
./test-functional.sh
```

如果接口地址不是默认值：

```bash
BASE_URL="http://localhost:8080/api" ./test-functional.sh
```

## 5. 结果判定

脚本执行成功时，末尾应输出：

```text
[PASS] 功能性测试完成
```

如果任一步骤失败，脚本会立即退出，并输出失败原因。

## 6. 说明与限制

- 当前脚本为了减少环境依赖，使用 `python3` 解析 JSON，而不是依赖 `jq`
- 入库测试会真实新增一条测试批次数据，适合演示环境或测试环境使用
- 医生开药会真实生成一张处方，并可能触发库存扣减
- 药师越权测试是否返回 `4010 unauthorized`，取决于所选药品是否属于处方药
- 如果需要稳定验证“药师开具处方药必失败”，建议将脚本中的 `TEST_DRUG_ID` 固定为当前环境中已知的处方药 ID，再执行

## 7. 建议扩展

后续如需继续完善脚本，建议优先补充以下断言：

- 入库记录查询，确认流水生成成功
- 医生开药后库存数量的精确扣减校验
- 库存不足时开药失败场景
- 药师固定针对 `category='处方药'` 药品发起请求并断言 `4010`
