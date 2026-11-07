#!/usr/bin/env python3
"""Android vitals of every app on Google Play, as one Markdown table.

Reads the Play Developer Reporting API with the same service account as play_status.py: the
user-perceived crash, ANR, low-memory-kill and slow-start rates over the last 28 days, next to
Google's bad-behaviour thresholds. Apps with too few users get no data from Google ("—").

  .venv/bin/python scripts/play_vitals.py
"""

from datetime import date, timedelta

import requests

from play_status import credentials, repo_apps

API = "https://playdeveloperreporting.googleapis.com/v1beta1/apps/"
SCOPE = ["https://www.googleapis.com/auth/playdeveloperreporting"]

# (column, metric set, metric, Google's overall bad-behaviour threshold or None, extra dimensions)
METRICS = [
    ("Crashes", "crashRateMetricSet", "userPerceivedCrashRate28dUserWeighted", 0.0109, []),
    ("ANRs", "anrRateMetricSet", "userPerceivedAnrRate28dUserWeighted", 0.0047, []),
    ("Low-memory kills", "lmkRateMetricSet", "userPerceivedLmkRate28dUserWeighted", None, []),
    ("Slow cold starts", "slowStartRateMetricSet", "slowStartRate28dUserWeighted", None, ["startType"]),
]


def latest_day(session, package, metric_set):
    """Last full day with data: the API's daily freshness is an exclusive end."""
    info = session.get(API + package + "/" + metric_set).json()
    for freshness in info.get("freshnessInfo", {}).get("freshnesses", []):
        if freshness["aggregationPeriod"] == "DAILY":
            end = freshness["latestEndTime"]
            return date(end["year"], end["month"], end["day"]) - timedelta(days=1)
    return None


def query(session, package, metric_set, metric, dimensions):
    """{dimension value or "": rate} for the last day of data, holding the 28-day rate."""
    day = latest_day(session, package, metric_set)
    if day is None:
        return {}
    end = day + timedelta(days=1)
    tz = {"id": "America/Los_Angeles"}
    body = {
        "timelineSpec": {
            "aggregationPeriod": "DAILY",
            "startTime": {"year": day.year, "month": day.month, "day": day.day, "timeZone": tz},
            "endTime": {"year": end.year, "month": end.month, "day": end.day, "timeZone": tz},
        },
        "metrics": [metric],
        "dimensions": dimensions,
    }
    response = session.post(API + package + "/" + metric_set + ":query", json=body)
    response.raise_for_status()
    rates = {}
    for row in response.json().get("rows", []):
        key = "/".join(d.get("stringValue", "") for d in row.get("dimensions", []))
        for value in row.get("metrics", []):
            if "decimalValue" in value:
                rates[key] = float(value["decimalValue"]["value"])
    return rates


def cell(rates, threshold):
    if not rates:
        return "—"
    parts = []
    for key, rate in sorted(rates.items()):
        text = f"{rate:.2%}"
        if threshold is not None:
            text += " ⚠️" if rate >= threshold else " ✅"
        parts.append(f"{key.lower()} {text}" if key else text)
    return ", ".join(parts)


def main():
    session = requests.Session()
    session.headers["Authorization"] = "Bearer " + credentials(SCOPE).token
    print("| App | " + " | ".join(column for column, *_ in METRICS) + " |")
    print("|---|" + "---|" * len(METRICS))
    for folder, package, _, _ in repo_apps():
        cells = []
        for _, metric_set, metric, threshold, dimensions in METRICS:
            try:
                cells.append(cell(query(session, package, metric_set, metric, dimensions), threshold))
            except requests.HTTPError as error:
                cells.append(f"error {error.response.status_code}")
        print(f"| {folder} | " + " | ".join(cells) + " |")
    print()
    print("User-perceived rates over the last 28 days. Thresholds (overall): crashes 1.09 %, ANRs 0.47 %. "
          "— = not enough users for Google to report, or not on Play.")


if __name__ == "__main__":
    main()
