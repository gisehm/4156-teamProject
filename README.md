# Meal Planning Service

This is the accept_cookies repository for our Meal Planning Service (COMS-4156 Fall 2026)

## Building and Running a Local Instance

In order to build and use this service you must install the following (This guide assumes Mac but the Maven README has instructions for both Windows and Mac):

1. Maven 3.10.0 (or latest version): https://maven.apache.org/download.cgi Download and follow the installation instructions depending on your system (Windows, Mac, etc).

2. JDK 25: This project used JDK 25 for development so that is what we recommend you use: https://www.oracle.com/java/technologies/downloads/#java25

3. VSCode IDE: I recommend using Visual Studio Code but you are free to use any other IDE that you are comfortable with: https://code.visualstudio.com/download?_exp_download=fb315fc982

4. When you open VSCode you have the option to clone from a GitHub repo; paste the following in your terminal
```git clone https://github.com/gisehm/4156-teamProject.git```.

5. In order to build the project with maven you can run ```mvn spring-boot:run```. You can run the tests either via the test files described below or by running ```mvn test``` from the ```IndividualProject``` folder.

6. If you wish to run the style checker you can with ```mvn checkstyle:check``` or ```mvn checkstyle:checkstyle``` if you wish to generate the report.

## API Endpoints

All endpoints are under `/v1`. Every endpoint except `POST /v1/clients` needs an `X-API-Key` header holding the API key of a registered client. A client can only reach its own data: its key must match the `client_id` in the URL. Errors are returned as JSON in the form `{"error": "<message>"}`.

Each endpoint below lists its inputs, outputs, side effects, status codes, and any required call order.

### POST /v1/clients/{client_id}/mealplan (local fixture integration)

With both `local` and `mealplan-demo` profiles active, POST creates a plan
using synthetic recipes and saves the shared `MealPlan` to `mealplans.json`.
The returned `id` can be passed directly to GET below. The request uses
`time_frame`, `max_calories`, `diet_type`, `allergens`, and
`max_prep_time_mins`; the response uses the shared model's camelCase fields.
See [local POST setup, contract, and limitations](MealPlanningService/docs/mealplan-post-local.md).
This does not yet implement real catalog nutrition or client-library recipes.

### GET /v1/clients/{client_id}/mealplan/{mealplan_id}

Retrieves one saved meal plan.

**Inputs**

| Where | Name | Description |
| --- | --- | --- |
| Header | `X-API-Key` | The API key of the client that owns the plan |
| Path | `client_id` | The ID of the client that owns the plan |
| Path | `mealplan_id` | The ID of the meal plan to retrieve |

**Output (200)**: the stored meal plan as JSON.

| Field | Description |
| --- | --- |
| `id` | The meal plan ID |
| `clientId` | The ID of the client that owns the plan |
| `timeFrame` | `day` or `week` |
| `diet` | The diets the plan was built for (list) |
| `allergens` | The allergens the plan avoids (list) |
| `maxCalories` | The most calories allowed per day, or `null` if there was no limit |
| `maxPrepTimeMins` | The longest preparation time in minutes, or `null` if there was no limit |
| `schedule` | A list of days. Each has `day` and the three meals `breakfast`, `lunch` and `dinner` |

Each meal holds `sourceRecipeId`, `mealType`, `calories` and `totalTimeMinutes`. Recipes are referenced by ID, so full recipe details (name, ingredients) are not repeated inside the plan.

**Side effects**: none. This is a read-only call.

**Status codes**

| Code | When |
| --- | --- |
| 200 | The plan exists and belongs to the client |
| 401 | The `X-API-Key` header is missing or is not a registered key |
| 403 | The key is valid but belongs to a different client than `client_id` in the path |
| 404 | The client has no meal plan with that ID. Another client's plan also returns 404, so plan IDs cannot be probed |
| 500 | A storage file could not be read |

**Call order**: register the client first (`POST /v1/clients` returns its `client_id` and `api_key`). In the local fixture integration above, POST then creates the plan. Other profiles require an existing plan in `mealplans.json` (a list of meal plans, empty by default).

**Example scenario**

1. Start the service: `mvn spring-boot:run` from the `MealPlanningService` folder (it listens on `http://127.0.0.1:8080`).
2. Add a plan owned by a registered client to `mealplans.json`, for example one with `"id": "demo-plan-1"` and `"clientId"` set to that client's ID from `clients.json`.
3. Request it with that client's ID and API key:

```
curl http://127.0.0.1:8080/v1/clients/b649544e-c51d-4e89-8f3b-cc83f37e8c49/mealplan/demo-plan-1 \
  -H "X-API-Key: 540ba996-2ae9-4f72-8b4f-014b463bc4d7"
```

Response (`200`):

```
{"id":"demo-plan-1","clientId":"b649544e-c51d-4e89-8f3b-cc83f37e8c49","timeFrame":"day","diet":["Vegetarian"],"allergens":["peanuts"],"maxCalories":1800,"maxPrepTimeMins":40,"schedule":[{"day":"MONDAY","breakfast":{"sourceRecipeId":"r1","mealType":"BREAKFAST","calories":400,"totalTimeMinutes":10},"lunch":{"sourceRecipeId":"r2","mealType":"LUNCH","calories":600,"totalTimeMinutes":20},"dinner":{"sourceRecipeId":"r3","mealType":"DINNER","calories":700,"totalTimeMinutes":30}}]}
```

**Ways it should not work** (each returns an error and changes nothing):

| Request | Result | Why |
| --- | --- | --- |
| No `X-API-Key` header | 401 `{"error":"a valid X-API-Key header is required"}` | The service cannot tell who is asking |
| An unknown API key | 401 (same body) | The key does not belong to any registered client |
| A valid key from client B on client A's URL | 403 `{"error":"this API key cannot access that client"}` | A client may only read its own data |
| Client B's key and client B's ID, but client A's plan ID | 404 `{"error":"meal plan not found"}` | The plan is not B's, and the service does not reveal that it exists |
| A plan ID that does not exist | 404 `{"error":"meal plan not found"}` | There is nothing to return |

## Running Tests

The unit tests are located under the directory ```MealPlanningService/src/test/java/com/coms4156/mealplanningservice```.

From there, you can press the 'Run' button next to any ```@Test``` section to see the results.

## Style Checking Report

We used the tool "checkstyle" to check the style of our code and generate style checking reports.

## Bug Testing
Utilized PMD for MacOS to squash bugs (https://pmd.github.io/). 

Within the `MealPlanningService` folder run this in terminal: `pmd check -d src -R rulesets/java/quickstart.xml -f text`

## Static Code Analysis

We used PMD to perform static analysis on our codebase,

## Meal-plan generator review

The meal-selection algorithm remains independent of HTTP and storage. See the
[algorithm scope and verification](MealPlanningService/docs/mealplan-generator.md).
With JDK 25 selected, run `sh mvnw clean verify` from `MealPlanningService`.
