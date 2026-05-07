import requests
import time
import statistics

BASE_URL = "http://127.0.0.1:5000"

def benchmark(endpoint, payload):
    times = []

    # 🔥 WARM-UP (IMPORTANT)
    requests.post(f"{BASE_URL}{endpoint}", json=payload)

    for i in range(50):
        start = time.time()

        requests.post(f"{BASE_URL}{endpoint}", json=payload)

        end = time.time()
        times.append((end - start) * 1000)

    times.sort()

    p50 = times[int(0.5 * len(times))]
    p95 = times[int(0.95 * len(times))]
    p99 = times[int(0.99 * len(times))]

    print(f"\n--- {endpoint} ---")
    print(f"p50: {p50:.2f} ms")
    print(f"p95: {p95:.2f} ms")
    print(f"p99: {p99:.2f} ms")

# Run tests
benchmark("/categorise", {"text": "RBI compliance rules"})
benchmark("/query", {"question": "What is compliance?"})
benchmark("/generate-report", {"text": "RBI compliance rules for banks"})