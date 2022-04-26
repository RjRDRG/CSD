## Getting Started

> The repository already includes the latest build jars

- Start replicas and client: **./start.sh**
- Only start replicas: **docker compose up**
- Only start client: **java -jar ./client/target/client-0.0.1-SNAPSHOT.jar**

## Endpoint for acessing the servers database:
>https://localhost:8080/h2

>https://localhost:8081/h2

>https://localhost:8082/h2

>https://localhost:8083/h2

    url := jdbc:h2:file:./ledger
    username := sa
    password :=
