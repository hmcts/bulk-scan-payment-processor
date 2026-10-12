# bulk-scan-payment-processor

## Purpose

The Bulk Scan Payment Processor retrieves service bus queue messages from the payments queue and processes them by either:

- Creating a new payment record for an exception record in CCD.
- Updating an existing CCD exception record reference in payments to use the service case reference.

## Getting Started
### Prerequisites

- [JDK 25](https://www.oracle.com/java)
- Project requires Spring Boot v3.x to be present

## Quick Start
An alternative faster way getting started is by using the automated setup script. This script will help set up all
bulk scan/print repos including bulk-scan-payment-processor and its dependencies.
See [common-dev-env-bsbp](https://github.com/hmcts/common-dev-env-bsbp) repository for more information.

### Development container (IntelliJ)

The devcontainer provides Java 25, GitHub CLI and Copilot CLI, using the same
setup as bsp-cron-trigger. Start Docker Desktop and create the repository's `.env`
using the existing common-dev-env setup before opening the container.

1. Open this repository in IntelliJ.
2. Open `.devcontainer/devcontainer.json`.
3. Use the gutter action **Create Dev Container and Mount Sources**.
4. Select **Build Container and Continue**.
5. Connect to the IntelliJ backend with JetBrains Client.

Mount Sources uses your current checkout. The container reads the existing `.env`
and forwards application port 8583. For dependencies running on your Mac through
common-dev-env, use `host.docker.internal` instead of `localhost` in service URLs.

Set the project SDK, Gradle JVM and application run configuration JRE to the
container's Java 25 installation. Run local checks with:

```bash
./gradlew clean build integration compileFunctionalTestJava compileSmokeTestJava jacocoTestReport
```

Integration tests use the repository's WireMock configuration. Functional and
smoke tests require their configured services and credentials.

Install the IntelliJ Copilot plugin through **Settings → Plugins** after connecting.
Automatic plugin installation is omitted because it caused a backend restart loop
in bsp-cron-trigger. Copilot CLI is installed by the container setup command.

The `devcontainer-home` volume preserves GitHub CLI and Copilot CLI settings.
The host Git configuration is mounted read-only and the host SSH agent is forwarded.
Commit and tag signing are disabled inside the container, matching the BSP cron setup.
Feature versions are recorded in `.devcontainer/devcontainer-lock.json`.

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

