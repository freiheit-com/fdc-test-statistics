# Change Log
All notable changes to this project will be documented in this file.
This project adheres to [Semantic Versioning](http://semver.org/).

## [0.11.0] - 2018-03-19
### Added
- REST-API for deployment tracking
- BigQuery configuration

## [0.10.1] - 2017-10-10
### Changed
- Introduced project tokens
- allow project tokens to get and push statistics

## [0.10.0] - 2016-03-06
### Changed
- Update to Clojure 1.8.0
- /statistics/coverage/diff endpoint to /statistics/diff/coverage
- Rewrite of project coverage aggregation: goes not arbitrary back in time and
  is not limited to one week/month anymore.

## [0.9.3] - 2016-02-29
## Fixed
- statistics date route

## [0.9.2] - 2016-02-28
## Fixed
- overwrite of data on pushing twice on a day

## [0.8.0] - 2016-02-14
### Added
- expand meta/projects call with details
- add call to fetch subproject and language coverage
- unit and integration tests
- add h2 in memory db for tests
- coverage diff can now go back arbitrary days (../coverage/diff/<project-name>/days/<day-to-go-back>)
### Changed
- refactoring the whole db layer

## [0.7.1] - 2016-01-23
### Added
- line and covered diff data in /statistics/coverage/diff
- changelog

##[0.7.0] - 2016-01-21
### Added
- REST-API for coverage diff (/statistics/coverage/diff/<project-name>)
