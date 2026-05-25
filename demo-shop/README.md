# demo-shop

Micronaut demo app for the `casciian-micronaut-starter`.

- **Customers** browse products at <http://localhost:8080/>.
- **Operators** can administer the same in-memory catalogue over:
  - SSH (`ssh admin@localhost -p 2222`, password `admin`)
  - Unix domain socket console mode: `java -jar demo-shop-*.jar console`

## Run

```sh
./gradlew :demo-shop:run
```

## Build native executable

```sh
./gradlew :demo-shop:nativeCompile
```

The native image binary is produced under
`demo-shop/build/native/nativeCompile/`.
