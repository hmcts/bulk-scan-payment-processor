# bulk-scan-payment-processor

## Purpose

The Bulk Scan Payment Processor retrieves service bus queue messages from the payments queue and processes them by either:

- Creating a new payment record for an exception record in CCD
- Updating an existing CCD exception record reference in payments to use the service case reference.

## Getting Started
### Prerequisites

- [JDK 21](https://www.oracle.com/java)
- Project requires Spring Boot v3.x to be present

## Quick Start
An alternative faster way getting started is by using the automated setup script. This script will help set up all
bulk scan/print repos including bulk-scan-payment-processor and its dependencies.
See [common-dev-env-bsbp](https://github.com/hmcts/common-dev-env-bsbp) repository for more information.

## Building and deploying the application

### Building the application

The project uses [Gradle](https://gradle.org) as a build tool. It already contains
`./gradlew` wrapper script, so there's no need to install gradle.

To build the project execute the following command:

```bash
  ./gradlew build
```

### Running the application

Create the image of the application by executing the following command:

```bash
  ./gradlew assemble
```

Create docker image:

```bash
  docker-compose build
```

Run the distribution (created in `build/install/bulk-scan-payment-processor` directory)
by executing the following command:

```bash
  docker-compose up
```

This will start the API container exposing the application's port
(set to `8583` in this template app).

In order to test if the application is up, you can call its health endpoint:

```bash
  curl http://localhost:8583/health
```

You should get a response similar to this:

```
  {"status":"UP","diskSpace":{"status":"UP","total":249644974080,"free":137188298752,"threshold":10485760}}
```

### Alternative script to run application

To skip all the setting up and building, just execute the following command:

```bash
./bin/run-in-docker.sh
```

For more information:

```bash
./bin/run-in-docker.sh -h
```

Script includes bare minimum environment variables necessary to start api instance. Whenever any variable is changed or any other script regarding docker image/container build, the suggested way to ensure all is cleaned up properly is by this command:

```bash
docker-compose rm
```

It clears stopped containers correctly. Might consider removing clutter of images too, especially the ones fiddled with:

```bash
docker images

docker image rm <image-id>
```


There is no need to remove java or similar core images.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details
