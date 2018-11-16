## Running/Developing Locally

### Infrastructure

The supporting infrastructure, such as etcd and kafka, can be started by running

```
cd dev/telemetry-infra
docker-compose up -d
```

You can stop the infrastructure by running the following in that same directory:

```
docker-compose down
```

Add `-v` to that invocation to also remove the volumes allocated for the services.

You can watch the logs of any (or all) of the services in the composition using:

```
docker-compose logs -f service ...
```

such as

```
docker-compose logs -f kafka
```

### Consuming from a kafka topic

To verify topic content is flowing and correct, you can exec into the kafka container and
run the standard console consumer, such as

```bash
docker exec -it telemetry-infra_kafka_1 \
  kafka-console-consumer --bootstrap-server localhost:9093 --topic telemetry.metrics.json
```

**NOTE** the use of port 9093 instead of 9092

### Applications

_**NOTE** The following procedure is IntelliJ specific but the process will be similar for other IDEs._

To open the project in IntelliJ, use the "open" option from the intro window, (or the File->Open dropdown).  Do not use either "Create New Project" or "Import Project" options, as those will misconfigure the project.  Open the root directory of this project, (the same one this readme is located in.)


In the "Maven Projects" tab (usually on right side of IDE window), click the "Generate Sources and Update Folders"
button to generate the protobuf/grpc code located in the `telemetry-protocol` module.

Create an **Ambassador** run configuration 
1. Locate the file `apps/ambassador/src/main/java/com/rackspace/rmii/telemetry/ambassador/TelemetryAmbassadorApplication.java`
2. Right-click the file and choose the "Create" option just below the build and run options
3. In the "Working Directory" field, enter `dev`
4. Save the configuration

Create an **API** run configuration
1. Locate the file `apps/api/src/main/java/com/rackspace/rmii/telemetry/api/TelemetryApiApplication.java`
2. Right-click the file and choose the "Create" option just below the build and run options
3. Save the configuration

Create an **AuthService** run configuration
1. Locate the file `apps/auth-service/src/main/java/com/rackspace/rmii/authservice/TelemetryAuthServiceApplication.java`
2. Right-click the file and choose the "Create" option just below the build and run options
3. Set Active Profile to "dev".
4. Set the following Override Parameters to the values returned by the setup-vault.sh script:
```
  vault.app-role.role-id
  vault.app-role.secret-id
```

5. Save the configuration

Launch each of the run configurations by choosing it from the drop down in the top-right of the IDE window
and clicking the "Run" or "Debug" button to launch in the respective mode. _I recommend using debug mode in
most cases since you can add breakpoints on the fly._

### Running applications via Maven

You can also run the Ambassador, and API modules via Maven by `cd`ing
into the respective module and invoking:

```bash
mvn spring-boot:run
```
You also run the Auth service via Maven with this command:
```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--spring.profiles.active=dev
```
You'll need to set the secret-id and role-id in rmii-telemetry-core/telemetry-auth-service/src/main/resources/application-dev.yml as mentioned above.

The configuration in their `pom.xml` will take care of setting the working directory and
a debug log level for our code.

### Building local Docker images

When the Maven profile "docker" is activated, the ambassador and api modules will produce
a Docker image as part of the `package` goal.

In the ambassador, api, and auth-service module directories, run the following to build a docker image:

```bash
mvn -P docker package
```

### Setting up Vault for development usage

The Vault server itself is already included in the Docker composition you would have
started above.

To setup the secrets and auth engines used by our applications you can run the script:

```bash
dev/telemetry-infra/setup-vault.sh
```

**NOTE** If you haven't built the support modules previously, you'll need to run a
`mvn install` in the top-level `salus-telemetry-bundle` directory.

### Running the applications in Docker with Repose/Identity Auth

First, ensure you have locally built the latest docker images by referring to the section above.

Export the following environment variables (or set them in IntelliJ's Docker Compose run config):

```bash
export VAULT_ROLE_ID=...
export VAULT_SECRET_ID=...
export KEYSTONE_USER=...
export KEYSTONE_PASSWORD=...
```

The "VAULT" values will be obtained from the `setup-vault.sh` usage, above.

The "KEYSTONE" values can be obtained by looking at the `<identity-service>` block of the
`/etc/repose/keystone-v2.cfg.xml` on any of the ele API nodes. _NOTE: we need a better place for that_

With those set, execute the following in the `dev/telemetry-apps` directory:

```bash
docker-compose up -d
```

The Envoy config file `dev/envoy-config-authserv-keystone.yml` can be used with this full set of
containers; however, you'll need to declare two more environment variables:

```bash
export ENVOY_KEYSTONE_USERNAME=...
export ENVOY_KEYSTONE_APIKEY=...
```

## Recommended IntelliJ Plugins

* .env files support (0.7)
* Apache Avro™ support (0.3.1)
* BashSupport (1.6.13.182)
* Docker integration (182.4323.18)
* Go (182.4129.55.890)
* [HashiCorp Terraform / HCL language support](https://plugins.jetbrains.com/plugin/7808-hashicorp-terraform--hcl-language-support)
* JS GraphQL (1.7.2)
* Kubernetes (182.3588)
* Lombok Plugin (0.19-LATEST-EAP-SNAPSHOT)
* Lua (1.0.114)
* Makefile support (1.3)
* Markdown support (182.4505.7)
* Maven Helper (3.7.172.1454.3)
* Protobuf Support (0.11.0)
* [RegexpTester](https://plugins.jetbrains.com/plugin/2917-regexptester)
* Spring Boot (1.0)
* Toml (0.2.0.19)

## Publishing Docker images to GCR

Use [the preparation part of these docs](https://cloud.google.com/container-registry/docs/pushing-and-pulling) 
to install the Cloud SDK tools and configure Docker for authentication. You can disregard the 
details about `docker push` since the [Maven jib plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin)
will take of the equivalent operations.

**Tip:** on MacOS you can install the Cloud SDK using brew:

```bash
brew cask install google-cloud-sdk
```

In each of the application modules, run the following replacing `$PROJECT_ID` with the Google
Cloud project's ID, which is of the form of an identifer and number separated by a dash:

```
mvn -P docker -Ddocker.image.prefix=gcr.io/$PROJECT_ID deploy
```

If publishing a snapshot version of the Maven projects, then add

```
-Dmaven.deploy.skip=true
```

to skip the Bintray publishing of the Maven artifacts.

If the local system doesn't have Docker installed, you can still perform the remote publish and
skip the local Docker build by adding:

```
-DskipLocalDockerBuild=true
```

## Publishing Java artifacts to Bintray

For non-SNAPSHOT builds of the Java applications, a `mvn deploy` can publish the built artifacts
to Bintray; however, you will need to declare your Bintray access credentials in
`$HOME/.m2/settings.xml` as shown in the following, but replacing `BINTRAY_USERNAME` and
`BINTRAY_APIKEY` accordingly.

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      https://maven.apache.org/xsd/settings-1.0.0.xsd">

  <servers>
    <server>
      <id>bintray-racker-maven</id>
      <username>BINTRAY_USERNAME</username>
      <password>BINTRAY_APIKEY</password>
    </server>
  </servers>
</settings>
```