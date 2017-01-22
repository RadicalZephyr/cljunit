# Change Log
All notable changes to this project will be documented in this
file. This change log follows the conventions
of [keepachangelog.com](http://keepachangelog.com/).

## [Unreleased]

-[Unreleased]: https://github.com/RadicalZephyr/cljunit/compare/0.2.0...HEAD

## 0.2.0
### Added

- Made implementation more re-loadable
- Ability to identify any test class with a `RunWith` annotation
- Ability to identify jUnit version 3 style `TestCase` classes
- New singular public interface for running tests
  `run-tests-in-classes`

### Changed

- Improved test header formatting

### Removed

- Reflections library as a dependency
- Previous public method `run-tests-in-packages`

[0.2.0]: https://github.com/RadicalZephyr/cljunit/compare/0.1.0...0.2.0


## [0.1.0] - 2016-06-08
### Added

- Extracted code from [boot-junit] library

[0.1.0]: https://github.com/RadicalZephyr/cljunit/compare/8b83be8...0.1.0
[boot-junit]: https://github.com/RadicalZephyr/boot-junit
