#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"

REPO_PATH="$SCRIPT_DIR/mikaRulings"
OUTPUT_DB="$REPO_DIR/app/src/main/assets/databases/rulings.db"
IMAGE_CACHE="$SCRIPT_DIR/image_urls_cache.json"
NAMES_CACHE="$SCRIPT_DIR/card_names_cache.json"
TEMP_DIR="$SCRIPT_DIR/tmp"

API_BASE="https://db.ygoprodeck.com/api/v7/cardinfo.php"
RATE_DELAY=0.05
LANGS=(fr de it pt)

check_deps() {
    for cmd in git jq sqlite3 curl; do
        if ! command -v "$cmd" &>/dev/null; then
            echo "Error: '$cmd' is required but not installed." >&2
            exit 1
        fi
    done
}

api_fetch() { curl -sS --connect-timeout 30 --max-time 120 "$1"; }

api_paginate() {
    local url="$1" jq_filter="$2" output_file="$3" page_size=500 offset=0 count=1
    :> "$output_file"
    while [ "$count" -gt 0 ]; do
        local page_url="${url}&num=${page_size}&offset=${offset}" resp
        resp="$(api_fetch "$page_url")"
        if echo "$resp" | jq -e '.error' >/dev/null 2>&1; then
            echo "  API error: $(echo "$resp" | jq -r '.error')" >&2; break
        fi
        count="$(echo "$resp" | jq '.meta.current_rows')"
        echo "$resp" | jq -c "$jq_filter" >> "$output_file"
        printf "." >&2; offset=$((offset + page_size)); sleep "$RATE_DELAY"
    done
    echo "" >&2
}

clone_or_pull() {
    if [ -d "$REPO_PATH/.git" ]; then
        echo "Pulling latest mikaRulings..."; git -C "$REPO_PATH" pull --quiet
    else
        echo "Cloning mikaRulings..."; git clone --quiet "https://github.com/MikaMikaDE/mikaRulings.git" "$REPO_PATH"
    fi
}

parse_cards() {
    local cards_dir="$REPO_PATH/cards" out="$TEMP_DIR/cards.jsonl" count=0
    echo "Parsing card JSONs..."; :> "$out"
    for f in "$cards_dir"/*.json; do
        jq -c '{
            id, name,
            type: (.Stats.type // ""),
            frameType: (.Stats.frameType // ""),
            atk: .Stats.atk, def: .Stats.def, level: .Stats.level,
            race: (.Stats.race // ""),
            attribute: (.Stats.attribute // "" | tostring),
            desc: (.Stats.desc // ""),
            rulings: .Rulings
        }' "$f" >> "$out"
        count=$((count + 1))
        [ $((count % 2000)) -eq 0 ] && echo "  Parsed $count cards..."
    done
    echo "Parsed $count cards total."
}

fetch_images() {
    local all_jsonl="$TEMP_DIR/api_all_cards.jsonl" map_json="$TEMP_DIR/image_map.json"
    echo "Loading image URL cache..."
    if [ -f "$IMAGE_CACHE" ]; then cp "$IMAGE_CACHE" "$map_json"; echo "  Loaded $(jq 'length' "$map_json") cached URLs."
    else echo '{}' > "$map_json"; fi

    echo -n "Fetching image URLs from YGOPRODeck API..."; api_paginate "$API_BASE?" '.data[] | {id, image_url: (.card_images[0].image_url // "")}' "$all_jsonl"

    echo "Merging image cache..."
    jq -s 'add' "$map_json" <(jq -s 'map({key: (.id|tostring), value: .image_url}) | from_entries' "$all_jsonl") > "${map_json}.new"
    mv "${map_json}.new" "$map_json"; cp "$map_json" "$IMAGE_CACHE"
    echo "  Total cached: $(jq 'length' "$map_json")"
}

fetch_translations() {
    local map_json="$TEMP_DIR/names_map.json"
    echo "Loading card names cache..."
    if [ -f "$NAMES_CACHE" ]; then cp "$NAMES_CACHE" "$map_json"; echo "  Loaded $(jq 'length' "$map_json") cached entries."
    else echo '{}' > "$map_json"; fi

    for lang in "${LANGS[@]}"; do
        local lang_jsonl="$TEMP_DIR/api_names_${lang}.jsonl"
        printf "Fetching %s card names..." "$lang" >&2
        api_paginate "$API_BASE?language=${lang}" '.data[] | {id, name}' "$lang_jsonl"

        local entries_json
        entries_json="$(jq -s --arg l "$lang" 'map({key: "\(.id)|\($l)", value: .name}) | from_entries' "$lang_jsonl")"
        jq -s 'add' "$map_json" <(echo "$entries_json") > "${map_json}.new"
        mv "${map_json}.new" "$map_json"
        printf " %s names\n" "$(wc -l < "$lang_jsonl")" >&2
        sleep "$RATE_DELAY"
    done

    cp "$map_json" "$NAMES_CACHE"
    echo "  Total cached: $(jq 'length' "$map_json")"
}

build_db() {
    local cards_jsonl="$TEMP_DIR/cards.jsonl" image_map="$TEMP_DIR/image_map.json"
    local names_map="$TEMP_DIR/names_map.json"

    echo "Building rulings.db..."
    mkdir -p "$(dirname "$OUTPUT_DB")"; rm -f "$OUTPUT_DB"

    echo "  Creating schema..."
    sqlite3 "$OUTPUT_DB" "
        PRAGMA journal_mode=OFF; PRAGMA synchronous=OFF; PRAGMA cache_size=-64000;
        CREATE TABLE cards(id INTEGER PRIMARY KEY NOT NULL, name TEXT NOT NULL, type TEXT NOT NULL,
            frameType TEXT NOT NULL, atk INTEGER, def INTEGER, level INTEGER, race TEXT NOT NULL,
            attribute TEXT, desc TEXT NOT NULL, image_url TEXT);
        CREATE TABLE rulings(card_id INTEGER NOT NULL, format TEXT NOT NULL, ruling_text TEXT NOT NULL,
            PRIMARY KEY(card_id, format));
        CREATE TABLE card_names(card_id INTEGER NOT NULL, lang TEXT NOT NULL, name TEXT NOT NULL,
            PRIMARY KEY(card_id, lang));
        CREATE VIRTUAL TABLE cards_fts USING fts4(content='cards', name);
        CREATE VIRTUAL TABLE card_names_fts USING fts4(content='card_names', name);
    "

    echo "  Generating cards..."
    jq -s --slurpfile imgs "$image_map" '
        ( $imgs[0] // {} ) as $img |
        .[] | [ .id, .name, .type, .frameType, .atk, .def, .level, .race, .attribute, .desc,
                ($img[.id|tostring] // "") ] | @csv
    ' "$cards_jsonl" > "$TEMP_DIR/cards.csv"

    echo "  Importing cards..."
    sqlite3 "$OUTPUT_DB" ".mode csv" ".import $TEMP_DIR/cards.csv cards"

    echo "  Generating rulings..."
    jq -r '.[] | . as $c | .rulings // {} | to_entries[] | [$c.id, .key, .value] | @csv' "$cards_jsonl" > "$TEMP_DIR/rulings_all.csv"

    local fmt_pattern
    fmt_pattern=$(echo "Goat|Edison|Tengu|REDU|HAT|Vegas|Meadowlands|FireWater|Cat|TeleDAD|Gladiator|ReturnDAD|Trooper|Circle|Stein|Base" | sed 's/|/\\|/g')
    grep -E "^[0-9]+,($fmt_pattern)," "$TEMP_DIR/rulings_all.csv" > "$TEMP_DIR/rulings.csv" || true

    echo "  Importing rulings..."
    sqlite3 "$OUTPUT_DB" ".mode csv" ".import $TEMP_DIR/rulings.csv rulings"

    echo "  Generating translated names..."
    jq -r 'to_entries[] | [ (.key | split("|")[0]), (.key | split("|")[1]), .value ] | @csv' \
        "$names_map" > "$TEMP_DIR/card_names.csv"

    echo "  Importing names..."
    sqlite3 "$OUTPUT_DB" ".mode csv" ".import $TEMP_DIR/card_names.csv card_names"

    local card_count ruling_count name_count
    card_count=$(sqlite3 "$OUTPUT_DB" "SELECT COUNT(*) FROM cards;")
    ruling_count=$(sqlite3 "$OUTPUT_DB" "SELECT COUNT(*) FROM rulings;")
    name_count=$(sqlite3 "$OUTPUT_DB" "SELECT COUNT(*) FROM card_names;")

    echo "  Rebuilding FTS indexes..."
    sqlite3 "$OUTPUT_DB" "INSERT INTO cards_fts(cards_fts) VALUES('rebuild');"
    sqlite3 "$OUTPUT_DB" "INSERT INTO card_names_fts(card_names_fts) VALUES('rebuild');"

    echo ""; echo "Done! rulings.db generated at $OUTPUT_DB"
    echo "Total cards: $card_count"; echo "Total rulings: $ruling_count"
    echo "Total translated names: $name_count"; echo "DB size: $(du -h "$OUTPUT_DB" | cut -f1)"
}

main() {
    echo "=== TurnZero Rulings DB Generator ==="; echo ""
    check_deps; mkdir -p "$TEMP_DIR"
    clone_or_pull; parse_cards; fetch_images; fetch_translations; build_db
    echo ""; echo "=== Complete ==="
}

main "$@"
