# casciian-micronaut

A [Micronaut 4.x](https://micronaut.io/) replica of
[`crramirez/casciian-spring`](https://github.com/crramirez/casciian-spring) —
same goals, same shape, but built on Micronaut's compile-time DI so the demo
ships as a real **GraalVM native executable** in addition to a regular JVM
JAR.

This Gradle multi-project hosts:

| Subproject | Purpose |
| ---------- | ------- |
| [`casciian-micronaut`](./casciian-micronaut) | Micronaut 4.x integration that exposes a Casciian TUI over SSH and/or a Unix domain socket. Publishable to Maven Central. |
| [`demo-shop`](./demo-shop) | Runnable Micronaut demo: a customer-facing web shop **and** an admin TUI that operate on the same H2-backed product catalogue. Produces both an executable JAR and a GraalVM native image. |

## Build

```sh
# Build everything (library + demo) and run the JUnit suite
./gradlew build

# Run the JVM demo
./gradlew :demo-shop:run

# Build the GraalVM native executable for the demo
# (requires a GraalVM 21 + native-image installation on PATH)
./gradlew :demo-shop:nativeCompile

# Then launch the native binary
./demo-shop/build/native/nativeCompile/demo-shop
```

## Using the running demo

Once `demo-shop` is up:

* Customers see the product catalogue at <http://localhost:8080/>.
* Operators run CRUD over the same database from a terminal in one of two ways:
  * Over SSH: `ssh admin@localhost -p 2222` (password `admin`).
  * Over a Unix domain socket (e.g. from inside the container via
    `docker exec` / `kubectl exec`): re-invoke the same JAR or native
    binary with the `console` argument — `java -jar build/libs/demo-shop-*.jar console`
    or `./demo-shop console` — and it acts as a thin terminal client
    that attaches to the running JVM via `/tmp/casciian.sock`.

See the per-subproject READMEs for details — most consumers of the
integration only need to read [`casciian-micronaut/README.md`](./casciian-micronaut/README.md).
