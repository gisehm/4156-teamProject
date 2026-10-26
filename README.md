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

This change adds the meal-selection algorithm and its focused tests for MPS-8.
It does not yet add the POST endpoint. See the
[algorithm scope and verification](MealPlanningService/docs/mealplan-generator.md).
With JDK 25 selected, run `sh mvnw clean verify` from `MealPlanningService`.
