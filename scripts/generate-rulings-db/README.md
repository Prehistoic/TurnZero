# Rulings DB Generator

Generates `app/src/main/assets/databases/rulings.db` from the mikaRulings GitHub repository
and YGOPRODeck API.

## Prerequisites
- JDK 17+
- Git

## Usage
```bash
cd scripts/generate-rulings-db
./gradlew run
```

## What it does
1. Clones/pulls `mikaRulings` repo from GitHub
2. Parses all card JSON files (card metadata + per-format rulings)
3. Fetches card image URLs from YGOPRODeck API (cached to `image_urls_cache.json`)
4. Generates `rulings.db` with `cards`, `rulings`, and `cards_fts` tables
5. Copies DB to `app/src/main/assets/databases/`

## Re-running
Subsequent runs only fetch image URLs for new cards. The `image_urls_cache.json`
file persists between runs. First run takes ~12 minutes (15K API calls at 20 req/sec).
