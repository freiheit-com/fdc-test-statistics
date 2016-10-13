[![Build Status](https://travis-ci.org/freiheit-com/fdc-test-statistics.svg?branch=master)](https://travis-ci.org/freiheit-com/fdc-test-statistics)

#REST-API:

## Coverage-Data

### Store coverage data:

    curl -kv -X PUT https://<servername>/publish/coverage -d '{"lines": <n>, "covered": <m>, "project": "<project-name>", "subproject": "<subproject-name>", "language": "<language>"}' -H "Content-Type: application/json" -H "auth-token: <publish-auth-token>"

Only the last PUT of the day is remembered by the statistic server.

### Get latest coverage data:

    curl -kv https://<servername>/statistics/coverage/latest/<project-name> -H "auth-token: <statistics-auth-token>"

Provides you with the most recent coverage data for project `<project-name>`. Goes back max. 30 days into the past (in order to search for coverage-data). Aggregates coverage data throughout subprojects and languages in `<project-name>`.

### Get coverage diff

    curl -kv https://<servername>/statistics/diff/coverage/<project-name> -H "auth-token: <statistics-auth-token>"

Returns a coverage diff in this JSON-format: `{"diff-percentage": 0.1, "diff-lines": 42, "diff-covered": 300}`
The diff is calculated between todays coverage data (as returned by `.../coverage/latest`) and the previous
workdays coverage data. Workday is used under the assumption that coverage data will not change on a weekend :)

## Project Management

Before project can push coverage data to the stastistic server they have to be registered. Project can managed via
this API:

### Register a project

    curl -kv -X PUT https://<servername>/meta/project -d '{"project": "<project-name>"", "subproject": "<subproject-name>", "language": "<language>"}' -H "Content-Type: application/json" -H "auth-token: <meta-auth-token>"

Register (project, sub-project, language) with the statistic server. You may not publish data before registering.

### Query all projects

    curl -kv https://<servername>/meta/projects -H "Content-Type: application/json" -H "auth-token: <meta-auth-token>"

Return a list of all projects known by the statistic server in the format: `{"projects": [{"project": "foo",
                  "subprojects": [{"subproject": "bar",
                                   "languages": [{"language": "java"}, {"language": "clojure"}]},
                                  {"subproject": "baz", "languages": ...}"`


#Build

lein ring uberjar (needs https://github.com/weavejester/lein-ring)

NOT lein uberjar (jars produced by this command do not contain the correct start code)
