#!/usr/bin/env bash
# Собирает bootJar каждого сервиса для последующего docker compose up --build.
set -euo pipefail

modules=(eureka-server auth-service booking-service payment-service notification-service gateway)

for module in "${modules[@]}"; do
  echo "==> Сборка $module"
  (cd "$module" && ./gradlew --no-daemon clean bootJar)
done

echo "Все модули собраны: ${modules[*]}"
