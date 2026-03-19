# Phase 7: Observability - Execution Plan

## Overview
Add Actuator + Prometheus metrics, and a Grafana dashboard via Docker Compose.

---

## 1. Dependencies

### Tasks
- [ ] Add `spring-boot-starter-actuator`
- [ ] Add `micrometer-registry-prometheus`

---

## 2. Actuator Configuration

### Goals
- Expose `/actuator/health`, `/actuator/info`, `/actuator/metrics`, `/actuator/prometheus`
- Keep base path `/actuator`
- Endpoints open (no auth) for this project

### Tasks
- [ ] Configure `management.endpoints.web.exposure.include`
- [ ] Confirm `/actuator/prometheus` works

---

## 3. Prometheus Setup

### Goals
- Add Prometheus container (default port 9090)
- Scrape app metrics at `/actuator/prometheus`

### Tasks
- [ ] Add `prometheus.yml` with scrape target
- [ ] Update `compose.yml` with Prometheus service

---

## 4. Grafana Setup

### Goals
- Add Grafana container (default port 3000)
- Provision Prometheus datasource
- Basic dashboard: latency, error rate, RPS

### Tasks
- [ ] Add Grafana service in `compose.yml`
- [ ] Add datasource provisioning
- [ ] Add basic dashboard JSON

---

## Verification

- [ ] `GET /actuator/health`
- [ ] Prometheus target shows UP
- [ ] Grafana dashboard loads metrics

---

## Notes

- Actuator endpoints are open (no auth). Mention this in Phase 8 docs.
