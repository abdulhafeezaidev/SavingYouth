# High-Scale System Scaling Architecture Design

This document details the architectural blueprints and synchronization policies designed to scale the **Behavioral Firewall** application safely and efficiently from **10K to 1M+ active users** without degrading device battery life, bloating Appwrite clusters, or violating user privacy.

---

## 💾 1. Local-First Fallback & Data Isolation Architecture

To guarantee total responsiveness and offline capability, the system is designed strictly around a **local-first** SQLite (SQLCipher-encrypted) model. 

```
                                [Active User Action]
                                         │
                                         ▼
                            [SQLCipher Local DB Write]
                                         │
                   ┌─────────────────────┴─────────────────────┐
                   ▼                                           ▼
          [Local UI Rendered]                       [Write Batch Queue]
          (Instant Response)                                   │
                                                     Is Wi-Fi & Charging?
                                                               │
                                                               ▼
                                                     [Compressed Bulk Sync]
                                                               │
                                                               ▼
                                                    [Appwrite Scale Cluster]
```

### Core Architecture Rules
1. **Device Autonomy**: All risk calculations, accessibility processing, and VPN interceptions operate strictly on-device without remote round-trips.
2. **Offline-First**: If the network goes offline, the app continues to operate at 100% feature fidelity. Telemetry events and anonymous logs are queued locally.

---

## 📦 2. Batching Strategies & Network Overhead Reductions

Writing every risk score or screen change event to a remote database immediately would drain device battery and crash servers during peak usage hours. The following optimizations are enforced:

### A. Local Telemetry Aggregation
* **Write Buffering**: App usage logs are buffered in memory and written to Room SQLite in batches every 5 minutes (or 50 logs, whichever comes first).
* **Hour-Level Compression**: Historical risk score trends are compressed on-device from detailed records to a single hourly average (e.g. `2026-05-24 15:00 -> avg: 42, max: 70`).

### B. Compressed Sync Bundles
* **Appwrite Sync Strategy**: Instead of syncing individual SQLite entities, the app serializes records into highly compressed gzip JSON arrays, reducing payload sizes by up to **85%**.
* **Upload Throttling**: Sync occurs strictly during charging and on Wi-Fi connection, unless the user manually triggers an emergency synchronization.

---

## 📈 3. Scalability Tiers: From 10K to 1M+ Active Users

| Metric / Parameter | Tier 1: 10K Users | Tier 2: 100K Users | Tier 3: 1M+ Users |
| :--- | :--- | :--- | :--- |
| **Database Model** | Single SQLite + Simple Appwrite | Partitioned SQLite + Cluster | Fully Aggregated Local DB + Scaled Cloud |
| **Peak Throughput** | 100 writes/sec | 1,000 writes/sec | 10,000+ writes/sec |
| **Batching Size** | 10 events / 1 minute | 50 events / 5 minutes | 100 events / 15 minutes |
| **Sync Window** | Every 4 hours (any network) | Every 12 hours (Wi-Fi preferred) | Every 24 hours (Wi-Fi + Charging only) |
| **Server Scaling** | Single Appwrite Droplet (4GB) | Scaled Appwrite (3 nodes + Load Balancer) | Distributed Kubernetes Cluster + Redis Caching |
| **Local Retention** | 30 days detailed logs | 14 days detailed logs | 7 days detailed logs (auto-pruned) |

---

## 🔄 4. Background Sync Windows & OS Scheduling Guidelines

To ensure absolute battery compliance and pass Google Play store performance audits:
- **WorkManager Scheduling**: Sync tasks must use `WorkManager` with `Constraints` indicating Wi-Fi and charging states.
- **Expedited Jobs**: Reserved strictly for high-urgency accountability notifications (such as accountability partner alert notifications).
- **AlarmManager Restrictions**: Strictly avoided for network syncing to prevent waking the device from low-power doze states.
