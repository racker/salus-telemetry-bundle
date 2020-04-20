This allows you to quickly spin up a Repose container that will proxy traffic from port `8180` to an instance of the Public API running locally (such as within an IDE / IntelliJ).

It can be used to test Repose config changes while being able to configure breakpoints in the application to see what headers are being passed down.

The `Local Repose` environment in the [Insomnia Workspace](https://github.com/racker/salus-telemetry-bundle/blob/master/dev/Insomnia-workspace.yaml) can be used for these requests.

## Usage

### Start Repose
Start the repose container with the command:
```
docker-compose up -d
```

### Configure the Public API
Assuming the `dev` profile is being used, the configuration will need to be modified to ensure it includes all authentication filters.  This can be done by removing the following section from the `application-dev.yml`:

```yaml
spring:
  profiles:
    include: "unsecured"
```

A couple other application properties will also need to be updated to override the defaults and be able to authenticate the required Keystone roles.

1. `SALUS_API_PUBLIC_ROLES`
   * [ApiPublicProperties](https://github.com/racker/salus-telemetry-api/blob/master/public/src/main/java/com/rackspace/salus/telemetry/api/config/ApiPublicProperties.java)
1. `SALUS_COMMON_ROLES_ROLETOVIEW`
   * [RoleProperties](https://github.com/racker/salus-common/blob/master/src/main/java/com/rackspace/salus/common/config/RoleProperties.java)

Each value will vary depending on what is being tested, but to replicate values already running in a Kubernetes environment first ensure `kubectl` is connected to the correct environment and then run:

```
kubectl describe deployment api-public
```

Locate the `SALUS_API_PUBLIC_ROLES` property and set the returned value in the relevant application-dev.yml configs.

```
kubectl get configmap salus-application-yml -o yaml
```

As `SALUS_COMMON_ROLES_ROLETOVIEW` is used across many applications it may be easiest to modify the `RoleProperties.java` file directly, but individual application-dev.yml files can be modified if desired.


### Start Appications
Within IntelliJ all relevant applications can now be started.  Any requests to localhost:8180 should now be proxied through to the local Public API.

## Stopping the container

```
docker-compose down -v
```