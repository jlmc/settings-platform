# Redis L1/L2 Shared Cache Architecture

This project implements a high-performance **Client-Side Caching (L1)** strategy integrated with a **Distributed Shared Cache (L2)** using Redis. This architecture is designed to drastically reduce read latency and overhead on the Redis server while maintaining data consistency across multiple application instances.

## 🚀 How the System Works

The architecture operates using two main layers:

1. **L2 (Shared Cache):** Redis acts as the centralized "source of truth" shared by all application instances.
2. **L1 (Local Cache):** Each application instance maintains a local copy of frequently accessed data in RAM using either `Caffeine` or a `ConcurrentHashMap`.

### Operational Flow:

* **Read Operation:** The application first attempts to read from the **L1** cache. If a *hit* occurs, data is returned in microseconds. On a *miss*, the system queries the **L2** (Redis), populates the **L1**, and returns the value.
* **Invalidation (Tracking):** When data is modified in Redis by any client, the Redis server sends a "push" notification to all instances holding that data in their L1, triggering an immediate eviction to ensure consistency.

## 📡 RESP3 Protocol

The use of the **RESP3** protocol is a critical technical requirement for this implementation.

* **Push Notifications:** Unlike RESP2, RESP3 allows the Redis server to send unsolicited (out-of-band) messages to the client over the same TCP connection.
* **Client Tracking:** We utilize the `CLIENT TRACKING` feature with **Broadcast (BCAST)** mode. This allows the application to subscribe to key prefixes (namespaces) and receive invalidation alerts without requiring the Redis server to track exactly which keys each client has in memory.

## 🛠 Minimum Redis Version

To support this architecture, the following are required:

* **Redis Server:** Version **6.0 or higher**. Support for the RESP3 protocol and Server-side tracking was introduced in this version.
* **Java Client:** Lettuce **6.x** or higher, explicitly configured to use `ProtocolVersion.RESP3`.

## 🏗 Implementation Structure

The architecture is organized around the `RedisSettingsProvider` interface, allowing for different strategies:

* **`RedisL1L2Caffeine`**: Recommended for production. Uses the Caffeine library to manage L1 with expiration (TTL) and size-limit policies.
* **`RedisL1L2SimpleMap`**: Uses a `ConcurrentHashMap`. Best suited for very small, static datasets.
* **`RedisL2OnlyService`**: Direct Redis access without L1, ensuring absolute consistency but incurring network latency on every call.

## 🛡 Resilience and Reconnection

The system is designed to be resilient to network failures:

* **Auto-Recovery:** A `RedisConnectionStateListener` monitors the connection status. Upon reconnection, the system automatically reactivates `CLIENT TRACKING`.
* **Consistency Safety:** Immediately following a reconnection, the L1 cache is fully cleared (`invalidateAll` or `clear`) to prevent the use of stale data that may have changed while the application was disconnected.
