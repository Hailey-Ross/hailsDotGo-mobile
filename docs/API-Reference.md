# API Reference

## Base URL

```
https://pogo.hails.live/
```

## Authentication

All endpoints except login require:

```
Authorization: Bearer <token>
```

The token is obtained from the login response and stored on-device in `EncryptedSharedPreferences` via `TokenStore`.

---

## Auth (AuthService.kt)

### POST /api/mobile/v1/auth/login

Authenticate and receive a session token.

**Request body:**
```json
{
  "username": "string",
  "password": "string"
}
```

**Response:**
```json
{
  "token": "string",
  "expires_at": "ISO8601 timestamp",
  "user": {
    "id": "integer",
    "username": "string",
    "role": "string",
    "lang": "string",
    "special_rank": "string or null",
    "trainer_level": "integer"
  }
}
```

---

### DELETE /api/mobile/v1/auth/session

Invalidate the current session token. No request body required.

---

### GET /api/mobile/v1/auth/me

Return the current authenticated user object. Response shape matches the `user` field from login.

---

### POST /api/mobile/v1/push/token

Register a device push token for FCM notifications.

**Request body:**
```json
{
  "platform": "android",
  "push_token": "string",
  "device_name": "string"
}
```

---

### DELETE /api/mobile/v1/push/token

Unregister a device push token on logout.

**Request body:**
```json
{
  "push_token": "string"
}
```

---

## IV (IVService.kt)

### POST /api/mobile/v1/iv/calculate

Submit scanned Pokemon stats and receive IV candidates.

**Request body:**
```json
{
  "pokemon_name": "string",
  "cp": "integer",
  "hp": "integer",
  "dust_cost": "integer",
  "trainer_level": "integer",
  "top_stat": "string or null",
  "appraisal_bars": "integer or null"
}
```

**Note on dust_cost:** The backend expects the base dust cost, not the displayed value. Lucky Pokemon display half the base cost; purified display 90%; shadow display 6x. `IVResultViewModel` normalizes the value before sending.

**Response:**
```json
{
  "candidates": [
    { "atk": "integer", "def": "integer", "sta": "integer", "level": "float", "rank": "integer" }
  ],
  "count": "integer",
  "pokemon": { "name": "string", "base_atk": "integer", "base_def": "integer", "base_sta": "integer" },
  "definitive": "boolean"
}
```

---

### GET /api/mobile/v1/data

Fetch static Pokemon data used for on-device CP arc calculations.

**Response:**
```json
{
  "pokemon": [ { "name": "string", "base_atk": "integer", "base_def": "integer", "base_sta": "integer" } ],
  "cpMultipliers": [ { "level": "float", "multiplier": "float" } ]
}
```

Results are cached by `PokemonDataRepository`.

---

### GET /api/mobile/v1/iv/pokemon

Fetch the authenticated user's Pokemon box.

**Response:**
```json
{
  "pokemon": [ { "id": "integer", "name": "string", "cp": "integer", "atk": "integer", "def": "integer", "sta": "integer", "level": "float" } ],
  "total": "integer"
}
```

---

### POST /api/mobile/v1/iv/pokemon

Save a confirmed IV result to the box.

---

### DELETE /api/mobile/v1/iv/pokemon/{id}

Remove a box entry by ID.

---

## Raid (RaidService.kt)

### GET /api/mobile/v1/raid/overview

Fetch active raid bosses and open lobbies.

**Response:**
```json
{
  "bosses": [ { "name": "string", "tier": "integer", "boosted": "boolean" } ],
  "lobbies": [ { "id": "integer", "boss_name": "string", "boss_tier": "integer", "member_count": "integer", "max_members": "integer" } ]
}
```

---

### GET /api/mobile/v1/raid/state

Return the current user's raid state.

**Response:**
```json
{
  "state": "idle | queued | matched | confirmed | raiding | reported",
  "lobby_id": "integer or null",
  "boss": "string or null",
  "confirm_deadline": "ISO8601 timestamp or null",
  "members": "array or null",
  "role": "host | member or null"
}
```

---

### POST /api/mobile/v1/raid/queue

Join the matchmaking queue.

**Request body:**
```json
{ "boss_name": "string", "boss_tier": "integer" }
```

---

### DELETE /api/mobile/v1/raid/queue

Leave the matchmaking queue.

---

### POST /api/mobile/v1/raid/lobbies

Create a new raid lobby.

**Request body:**
```json
{
  "boss_name": "string",
  "boss_tier": "integer",
  "note": "string or null",
  "max_members": "integer or null",
  "weather_boosted": "boolean or null"
}
```

---

### DELETE /api/mobile/v1/raid/lobbies/{id}

Cancel a lobby. Host only.

---

### POST /api/mobile/v1/raid/lobbies/{id}/confirm

Confirm attendance in a matched lobby.

---

### POST /api/mobile/v1/raid/lobbies/{id}/leave

Leave a lobby.

---

### POST /api/mobile/v1/raid/lobbies/{id}/kick

Remove a member from the lobby. Host only.

**Request body:**
```json
{ "user_id": "integer" }
```

---

### POST /api/mobile/v1/raid/lobbies/{id}/invited

Mark all members as invited in-game. Host only.

---

### POST /api/mobile/v1/raid/lobbies/{id}/report

Submit the post-raid attendance report.

**Request body:**
```json
{
  "attended": [ "integer" ],
  "left_early": [ "integer" ]
}
```

---

### POST /api/mobile/v1/raid/lobbies/{id}/feedback

Submit feedback for a completed raid.

**Request body:**
```json
{ "option_id": "integer" }
```

---

## Events (EventService.kt)

### GET /api/mobile/v1/events

Return a list of active and upcoming Pokemon GO events.

**Response:** Array of event objects with name, type, start, and end fields.
