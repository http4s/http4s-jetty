# http4s-jetty

## Compatibility

| http4s-jetty | http4s-core | jetty   | servlet-api | Scala 2.12 | Scala 2.13 | Scala 3 | Status    |
|:-------------|:------------|---------|:------------|------------|------------|---------|:----------|
| 0.22.x[^1]   | 0.22.x      | 9.x[^1] | 3.1         | ✅         | ✅         | ✅      | EOL       |
| 0.23.x       | 0.23.x      | 9.x     | 3.1         | ✅         | ✅         | ✅      | EOL       |
| 0.24.x       | 0.23.x      | 10.x    | 4.0         | ✅         | ✅         | ✅      | EOL       |
| 0.25.x       | 0.23.x      | 11.x    | 5.0         | ✅         | ✅         | ✅      | RC[^2]    |
| 0.26.x       | 0.23.x      | 12.x    | 4.0[^3]     | ✅         | ✅         | ✅      | Milestone |
| 0.27.x       | 0.23.x      | 12.x    | 5.0[^3]     | ✅         | ✅         | ✅      | Milestone |
| 0.28.x       | 0.23.x      | 12.x    | 6.0[^3]     | ✅         | ✅         | ✅      | Milestone |

[^1]: Additional artifacts named `http4s-jetty12-*` were published against Jetty 12 as a courtesy after Jetty 9 reached EOL.

[^2]: The underlying Jetty versions have reached EOL.  These will be given a proper release for any early adopters of the series, and then be EOL. We will check to make sure this is true, release the version, and then EOL. 12 will then be the current version where we will promote to RC.

[^3]: Support for servlet 5.0 and 6.0 is pending.
