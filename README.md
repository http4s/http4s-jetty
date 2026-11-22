# http4s-jetty

## Compatibility

| http4s-jetty | http4s-core | jetty   | servlet-api | Scala 2.12 | Scala 2.13 | Scala 3 | Status    |
|:-------------|:------------|---------|:------------|------------|------------|---------|:----------|
| 0.22.x[^1]   | 0.22.x      | 9.x[^1] | 3.1         | ✅         | ✅         | ✅      | EOL       |
| 0.23.x       | 0.23.x      | 9.x     | 3.1         | ✅         | ✅         | ✅      | EOL       |
| 0.24.x       | 0.23.x      | 10.x    | 4.0         | ✅         | ✅         | ✅      | EOL       |
| 0.25.x       | 0.23.x      | 11.x    | 5.0         | ✅         | ✅         | ✅      | EOL       |
| 0.26.x       | 0.23.x      | 12.x    | 4.0         | ✅         | ✅         | ✅      | Milestone |
| 0.27.x       | 0.23.x      | 12.x    | 5.0         | ✅         | ✅         | ✅      | Milestone |
| 0.28.x       | 0.23.x      | 12.x    | 6.0         | ✅         | ✅         | ✅      | Milestone |
| 0.29.x       | 0.23.x      | 12.x    | 6.1         | ✅         | ✅         | ✅      | Milestone |

[^1]: Additional artifacts named `http4s-jetty12-*` were published against Jetty 12 as a courtesy after Jetty 9 reached EOL.

### Jetty 12 environments

Jetty 12 core is independent of the Servlet API. Servlet support is provided by an environment-specific module:

| Jetty environment | Platform       | Servlet API | Package namespace    |
|:------------------|:---------------|:------------|:---------------------|
| `ee8`             | Java EE 8      | 4.0         | `javax.servlet.*`    |
| `ee9`             | Jakarta EE 9   | 5.0         | `jakarta.servlet.*`  |
| `ee10`            | Jakarta EE 10  | 6.0         | `jakarta.servlet.*`  |
| `ee11`            | Jakarta EE 11  | 6.1         | `jakarta.servlet.*`  |

See Jetty's [environment compatibility table](https://github.com/jetty/jetty-examples#all-example-branches) and [web application deployment documentation](https://jetty.org/docs/jetty/12.1/operations-guide/deploy/index.html) for details.
