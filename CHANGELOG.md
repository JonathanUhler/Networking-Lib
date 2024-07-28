# Networking-Lib Changelog

## Conventions
### Change Entries
New changelog entries are titled using the new version and the date of release. This information is
presented with a tag of the form `[MAJOR.Minor.path] - yyyy-MM-dd`. Each entry may contain up to
three sections, which generally categorize changes into:
* `Added`: Features that are new to this release. The significant content of the new version did
  not exist in previous versions. This generally indicates a `Minor` change at the least.
* `Fixed`: The major changes of the new version fix issues present in previous versions. The core
  API or correct, target functionality did not change. Alone, this generally indicates a
  `patch`-level change.
* `Changed`: Existing features were altered in some way: expanded upon, reduced in scope, or
  had internal implementations changed. Changes to existing features could realistically fall
  under any versioning scope, from algorithm optimizations to an entire API refactor.

### Versioning
Versions are assigned with the `MAJOR.Minor.patch` syntax, where the components of the version
string generally refer to the following scopes of changes:
* `MAJOR`: A breaking change that guarantees a **lack** of support with previous versions.
  That is, version 2.0.0 does **not** guarantee support of all the features of version 1.0.0, and
  the reverse case also does **not** guarantee support.
* `Minor`: A non-breaking but significant change. Compatibility of this version with an older
  version is guaranteed, but the reverse may not be true. That is, version 1.1.0 must support all
  the features of version 1.0.0, but the reverse may or may not be true.
* `patch`: A non-breaking change without any major significance (e.g. fixing typos, updating
  comments or documentation, algorithm optimization). That is, version 1.0.1 must support all the
  features of version 1.0.0, and version 1.0.0 must support all the features of version 1.0.1.

## [1.0.0] - 2024-07-28
### Added
* First full release of Networking-Lib in Java
