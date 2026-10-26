# Local POST to shared GET integration

This adds a fixture-based POST path to the team's shared `MealPlan`,
authentication, `StorageService`, and `mealplans.json`. It requires **both**
`local` and `mealplan-demo` profiles. Without them, this POST is unavailable.
GET reuses Nooraan's merged [PR #8](https://github.com/gisehm/4156-teamProject/pull/8).

## Contract

`POST /v1/clients/{client_id}/mealplan`, with the registered client's
`X-API-Key` and `Content-Type: application/json`:

```json
{
  "time_frame": "week",
  "max_calories": 1800,
  "diet_type": ["vegan"],
  "allergens": ["dairy"],
  "max_prep_time_mins": 40
}
```

The time frame must be `day` or `week`. Both numeric limits must be positive
32-bit integers; strings, decimals, and overflowing numbers are rejected.
Restrictions may be omitted or null, meaning empty lists. Duplicate tags are
deduplicated. Unknown fields, duplicate JSON keys, trailing JSON, and unsupported
restriction tags return 400.

Diet tags: `low_fat`, `high_protein`, `low_carb`, `vegetarian`, `vegan`.
Allergen tags: `peanuts`, `dairy`, `eggs`, `soy`, `wheat`, `tree_nuts`, `fish`,
`shellfish`, `sesame`. Tags are case-sensitive.

Success returns **201** with the shared model's fields: `id`, `clientId`,
`timeFrame`, `diet`, `allergens`, `maxCalories`, `maxPrepTimeMins`, `schedule`.
These are also the stored and GET response fields. `schedule` is the existing
`List<PlannedDay>`, not a new weekday-map representation. This local integration
follows the current shared model rather than the proposal's older response
example.

After the entire schedule succeeds, the service creates a UUID, constructs a
`MealPlan`, reads the existing **bare JSON array** in `mealplans.json`, appends
the plan, and writes it once. Existing plans are retained. The UUID and
`clientId` are what GET uses to find the plan. This is a single-process file
operation, not a database transaction or a distributed lock.

| Status | Meaning | Plan write |
| --- | --- | --- |
| 201 | Complete plan saved | One append |
| 400 | Invalid JSON or constraints | None |
| 401 | Missing or unknown API key | None |
| 403 | Key belongs to another client | None |
| 415 | Content-Type is not application/json | None |
| 422 | First unfillable day, included as `failed_day` | None |
| 500 | Existing data cannot be read or save fails | No success reported |

Invalid existing JSON is not replaced with an empty list. Authentication and
client ownership are checked before request parsing and generation.

## Local data and tests

Use JDK 25 and run `sh mvnw clean verify` in `MealPlanningService`.
The POST tests exercise the real GET controller and the shared JSON round-trip.

Start the built jar with:

```bash
java -jar target/MealPlanningService-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=local,mealplan-demo \
  --storage.local-directory=/absolute/path/to/private-demo-data \
  --server.address=127.0.0.1 --server.port=8082
```

Create `clients.json` and `mealplans.json` as `[]` in that private directory
only if they do not exist. Register through `POST /v1/clients`, use the returned
`client_id` and `api_key` for POST, then GET
`/v1/clients/{client_id}/mealplan/{id}` using the plan's returned `id`.
The optional `storage.local-directory` preserves the existing default directory
when omitted. A private directory avoids changing committed seed data.

## Fixture limitations

Each meal type has three synthetic plant-based choices (500 kcal each, 10,
11, or 12 minutes) and one synthetic dairy choice (500 kcal, 1 minute).
Plant fixtures have preset `vegetarian`, `vegan`, `low_fat`, `high_protein`
tags and no allergens; dairy fixtures have only `vegetarian` and contain dairy.
These tags and calories are test metadata, not nutritional verification.
With a calorie cap of 1800, every selected day totals 1500, within 1440–1800.
No fixture satisfies `low_carb`, so that request returns 422 on Monday.

This does not load the real catalog or client recipe library, calculate
nutrition from ingredients, parse real allergens, or save catalog recipe copies
into the client library. Those parts of MPS-8 remain unfinished. The returned
recipe IDs identify fixtures, not real catalog records. The local end-to-end
flow verifies model/storage compatibility and does not establish completion
of the full production POST requirements.

## Verification snapshot (October 26, 2026)

JDK 25 `clean verify`: 128 tests, zero failures, errors, or skips; the project's
configured Checkstyle passes. A separate check of the nine changed Java classes,
including the four new test classes, also reports zero violations. JaCoCo branch
coverage is 151/170 (88.82%), retaining the existing GCS exclusion.
Live localhost calls verified 201 creation followed by 200 GET with equal data,
retention of an earlier plan, ownership rejection, 400/401/403/422 failures
without plan changes, and corrupt-storage 500 without overwriting the bad data.
