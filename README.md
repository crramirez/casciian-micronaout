# casciian-micronaout

This Gradle multi-project mirrors the structure and intent of `casciian-spring`,
but targets **Micronaut 4.x**.

| Subproject | Purpose |
| ---------- | ------- |
| [`casciian-micronaut-starter`](./casciian-micronaut-starter) | Micronaut configuration + runtime services to expose a Casciian admin session over SSH and/or a Unix domain socket. |
| [`demo-shop`](./demo-shop) | Runnable Micronaut demo app with a customer-facing web catalogue plus an admin terminal session over the same in-memory repository. |

```sh
# Build all modules
./gradlew build

# Run the demo web + admin server
./gradlew :demo-shop:run

# Build native executable artifact (GraalVM)
./gradlew :demo-shop:nativeCompile
```

After starting the demo:

- Customer page: <http://localhost:8080/>
- Admin over SSH: `ssh admin@localhost -p 2222` (password `admin`)
- Admin over Unix socket console client:
  `java -jar demo-shop/build/libs/demo-shop-*.jar console`
