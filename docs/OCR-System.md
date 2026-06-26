# OCR System

## Overview

The OCR pipeline captures one screen frame, runs two passes of ML Kit text recognition, and applies four pixel-based detectors. All processing is on-device. No image data leaves the device.

---

## Screen capture

`ScreenCaptureService` is a foreground service using `foregroundServiceType="mediaProjection"`. Android 10+ requires the service to call `startForeground()` before any MediaProjection setup. The service creates a virtual display at the device's native resolution, reads one frame via `ImageReader`, converts it to a `Bitmap`, and immediately releases the projection. The bitmap is held in memory only for the duration of OCR processing.

---

## Two-pass ML Kit OCR

**Pass 1 (full image):** ML Kit Text Recognition runs on the full bitmap.

**Pass 2 (contrast crop):** The top 18% of the image is cropped, converted to greyscale, and contrast-boosted by a factor of 1.5, then ML Kit runs again. The reason for this second pass is that large Pokemon models can partially occlude the CP digits in Pass 1, causing an underread. The maximum CP value from both passes is used.

---

## Name extraction

Three strategies are tried in priority order. A higher-priority result will not be overwritten by a lower-priority one across accumulation cycles.

| Priority | Strategy | Pattern |
|---|---|---|
| 3 (highest) | Footer text | `This ([A-Z][a-z]+(?:[- ][A-Z][a-z]+)?)\s+was\s+caught` |
| 2 | Mega energy label | `([A-Za-z][A-Za-z-]{2,})\s+MEGA\s+ENERGY` |
| 1 (lowest) | Card zone (35-65% vertical) | Largest text block by pixel height, filtered against a blocklist of non-name words |

The current priority is tracked in `CaptureState.nameSourcePriority`.

---

## CP extraction

1. Crop the top 25% of the image.
2. Prefer any line of text that contains the substring "CP".
3. If no CP line is found, take the maximum numeric value in the crop zone.
4. Apply an OCR character-substitution fallback regex to handle common misreads (e.g., `O` read as `0`).

---

## HP extraction

Pattern: `(\d{1,4})\s*/\s*\d{1,4}\s*HP`

OCR commonly misreads the letter `O` and lowercase `o` as `0`. The extractor corrects this before matching.

---

## Dust cost extraction

The valid dust display values for each level range are precomputed into a set (including the lucky, shadow, and purified multiplied variants). The extractor matches the largest numeric value in the text against this set in descending order.

---

## Status flags

Lucky, Shadow, and Purified status are detected by two independent signals that are ORed:

1. Text pattern matching against the ML Kit output.
2. Dust cost math: purified costs 90% of the standard value, shadow costs 6x the standard value.

`PurifiedDetector` additionally scans pixel data (see below) as a third signal for Purified.

---

## Pixel detectors

### PurifiedDetector

Scans the zone from 50% to 63% of screen height and 0% to 45% of screen width at a step of 4 pixels. Counts pixels that match a bright blue/purple signature:

```
r > 170 && g > 150 && b > 200 && b >= r && (b - g) > 15
```

The `(b - g) > 15` filter eliminates the neutral white card background (where R approximately equals G approximately equals B). A count of 8 or more hits returns `true`.

---

### AppraisalBarDetector

Scans 13 horizontal rows from 58% to 70% of screen height across the left 25% of screen width. Uses a gap-based star-counting algorithm:

- A zero-hit row inside the orange span indicates that only the badge ring is present (1 or 2 stars).
- A continuous orange band (no gap) indicates the badge interior is filled (3 stars).

This distinguishes a 1-star badge (many orange hits but with a gap) from a 3-star badge (fewer hits, no gap).

Orange pixel condition:
```
r > 180 && g > 80 && b < 120 && r > g && (r - b) > 100
```

Rainbow/hundo detection looks for magenta (pink ring), purple, and teal pixels in the same zone. Three or more rainbow hits on a 3-star badge signals a hundo (100% IV).

---

### CPArcDetector

The CP arc is a circular progress bar around the Pokemon in the GO UI. The detector scans counterclockwise from the full-arc endpoint (96 degrees relative to the arc center) looking for the first bright pixel (RGB sum greater than 580). It converts the angular position to a fill percentage, then maps that percentage to a CP value using `maxCP(pokemonName, trainerLevel)` from `PokemonDataRepository`.

The arc geometry is calibrated for a 1080x2340 reference device. A known camera icon zone (x greater than 83%, y less than 18%) is skipped to avoid false positives.

---

## OCR accumulation

`CaptureState.setOCRResult()` merges each new scan into the accumulated state:

- Name: higher-priority source never overwritten by lower-priority source.
- CP: maximum value seen across all scans.
- Appraisal stars: maximum value seen.
- Boolean flags (lucky, shadow, purified, hundo): OR across all scans.

This allows the user to trigger multiple scans for better confidence without losing previously detected data.
