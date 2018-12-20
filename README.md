## Cloning the git submodules
```
git clone --recursive git@github.com:racker/salus-telemetry-bundle.git
```
### Pulling in submodule changes
Running `git submodule update --recursive` in the top level directory will set each submodule to the commit tagged in this repo.
```
$ git submodule update --recursive
Submodule path 'apps/ambassador': checked out '8fd6d17993001a0d5555f88dc1593ba56ff1ca4c'
Submodule path 'apps/salus-app-base': checked out 'c3b64afad4e2d31c775a7ce40803df1a7dc95630'

$ git submodule status
 8fd6d17993001a0d5555f88dc1593ba56ff1ca4c apps/ambassador (8fd6d17)
 0d434d08dc31c73918dc5a5b09d11ae503ae6f13 apps/api (heads/master)
 7fec0125daaa1d6554a405dab5e761ebfa98df0a apps/auth-service (heads/master)
 c999f7e314c3e267a8a9a343c60b8d7a23523e2e apps/envoy (0.1.1-39-gc999f7e)
 c3b64afad4e2d31c775a7ce40803df1a7dc95630 apps/salus-app-base (c3b64af)
 419171514ce8dc0d823ffe32431d255cdc684de6 libs/etcd-adapter (heads/master)
 8d3b86933450af562dc57c1fc2a9bf7005bf65b3 libs/model (heads/master)
```

If you wish to update all submodules to the head of their own master branch, you can append `--remote` to that command.
```
$ git submodule update --recursive --remote
...
...

$ git submodule status
+90b4454c51f1e045a4dd658a1c598ea2b7909391 apps/ambassador (remotes/origin/HEAD)
+af599bedbedab08c2c99e3700a37ab19ded5b649 apps/api (remotes/origin/HEAD)
+08fb8a1686addf8ecfe1ca9e212c4e26f0eac99a apps/auth-service (remotes/origin/HEAD)
 c999f7e314c3e267a8a9a343c60b8d7a23523e2e apps/envoy (0.2.0)
+c670acff016f58734e0dae7d5de815e173003052 apps/salus-app-base (heads/master)
+35587ab173d6a1d8e55a022233b6c0dcb9a39cda libs/etcd-adapter (remotes/origin/HEAD)
+9b54cba9ca40b560f333ea63c9644c2e18465c46 libs/model (remotes/origin/HEAD)
```


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

### Querying etcd

The etcd container includes the `etcdctl` command-line tool and is pre-configured to use the v3
API. You can perform operations with `etcdctl` via `docker exec`, such as:

```bash
docker exec -it telemetry-infra_etcd_1 etcdctl get --prefix /
```

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

### Maven usage for applications

The [app base README](apps/salus-app-base/README.md) contains information about how to build
and run the application modules with Maven.

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
