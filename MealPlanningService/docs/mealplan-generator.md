# Meal-plan generator

This is an algorithm-only slice of [MPS-8](https://accept-cookies.atlassian.net/browse/MPS-8),
not a completed POST endpoint.

## Inputs and behavior

The caller supplies eligible recipes with stable source IDs, meal types,
reliable per-serving calories, and preparation times. The generator:

- Groups recipes into breakfast, lunch, and dinner; merges identical source
  entries and rejects conflicting data for the same source.
- Sorts each group by preparation time, then source ID for a deterministic tie.
- Builds 1 or 7 days, requiring each day's total to be within the inclusive
  range of 80–100% of the daily calorie cap.
- Excludes recipes used in the preceding two days; a Monday recipe can be
  reused on Thursday.
- Accepts the first valid combination each day. It never revisits earlier
  days, so it can fail even when a different weekly arrangement would work.
- Returns a complete immutable schedule or throws an exception identifying the
  first unfillable day.

Monday as the starting label and the source-ID tie-break are implementation
defaults to confirm during team review. The core does not authenticate,
check ownership/diet/allergens/preparation limits, derive nutrition, expose
HTTP, or save recipes/plans. The endpoint must perform those steps and map
failure to its HTTP response. No production catalog or shared API model is
introduced here. Team review and integration remain pending.

## Verification

Use JDK 25. From `MealPlanningService`, run:

```sh
sh mvnw clean verify
```

The isolated build on October 25, 2026 used JDK 25.0.4.1 and passed 35 tests
(28 generator cases plus 7 team baseline cases), with no failures, errors,
or skips. Checkstyle reported zero violations. Generator branch coverage was
58/70 (82.86%); whole-project branch coverage was 62/74 (83.78%), following the
existing `GcsStorageService` exclusion. Build configuration was unchanged.

The tests exercise calorie boundaries and decimals, selection/tie order,
repeat exclusion and fourth-day reuse, duplicates, invalid inputs, immutable
results, and a greedy failure counterexample. Local evidence is under `target/`:
`mealplan-core-verify.log`, `surefire-reports/`, `checkstyle-result.xml`, and
`site/jacoco/index.html`. Build output is not included in this change.
