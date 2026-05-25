# demo-shop

A small Micronaut application that shows what the
[casciian-micronaut](../casciian-micronaut) integration is for, and
demonstrates that it works equally well in a **GraalVM native
executable**.

* **Customers** see a product catalogue at <http://localhost:8080/>.
* **Operators** SSH into the same JVM (`ssh admin@localhost -p 2222`,
  password `admin`) and CRUD the catalogue from a Casciian TUI.

Both views share the same `ProductRepository`, so anything an admin
creates, edits, or deletes from the terminal shows up the next time a
customer reloads the page.

## How it's wired

```
+--------------------------------------------------------+
|  Micronaut JVM / native image (DemoShopApplication)    |
|                                                        |
|  +-----------+     +-------------------+     +------+  |
|  |  Netty    | --> | ProductRepository | <-- | TUI  |  |
|  |  / Thymeleaf|   | (Micronaut Data   |    |  app  | |
|  +-----------+     |  JDBC)            |    +------+  |
|       |            +---------+---------+        |     |
|     :8080                    v               :2222    |
|                            H2 in-mem            (SSH) |
+--------------------------------------------------------+
```

* `Product` / `ProductRepository` &mdash; Micronaut Data `@MappedEntity`
  + JDBC `CrudRepository`.
* `ProductFakerSeeder` &mdash; uses [DataFaker][datafaker] to create
  ~25 random products on first startup (only when the table is empty;
  disabled in the `test` environment).
* `ShopController` &mdash; renders the customer-facing Thymeleaf page
  via [Micronaut Views][views].
* `admin/AdminTuiConfig` &mdash; declares the
  `CasciianTApplicationFactory` bean expected by the integration.
* `admin/AdminTApplication` &mdash; Casciian `TApplication` with a
  `Products` menu (Refresh / New / Edit selected / Delete selected) over
  the same `ProductRepository`.

## Run it

```sh
# from the repository root - JVM run
./gradlew :demo-shop:run

# …or build a GraalVM native executable (requires native-image on PATH)
./gradlew :demo-shop:nativeCompile
./demo-shop/build/native/nativeCompile/demo-shop
```

Then:

```sh
# customer view
open http://localhost:8080/

# admin view
ssh admin@localhost -p 2222    # password: admin
```

The admin TUI is keyboard-driven; press `F2` (or click) to open the
**Products** menu and use **New / Edit selected / Delete selected /
Refresh**. Use `F10` (or `File > Exit`) to log out.

If you can `exec` into the container that runs the demo, you can also
attach to the TUI through the Unix domain socket — no SSH client
required:

```sh
# inside the container, against the running JVM/native binary
java -jar /app/demo-shop.jar console     # JVM build
/app/demo-shop console                   # native build
```

## Configuration

See [`src/main/resources/application.yml`](src/main/resources/application.yml).
The H2 datasource is in-memory; SSH credentials default to `admin/admin`
for the demo and **must** be overridden for any real deployment, e.g.:

```sh
MICRONAUT_CONFIG_FILES=/etc/demo-shop/app.yml \
  java -jar demo-shop-*.jar
```

[datafaker]: https://www.datafaker.net/
[views]: https://micronaut-projects.github.io/micronaut-views/latest/guide/
