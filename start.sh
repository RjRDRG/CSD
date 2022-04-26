#!/bin/bash

docker compose up --build & java -jar -Dhttps.protocols=TLSv1.2 ./client/target/client-0.0.1-SNAPSHOT.jar
