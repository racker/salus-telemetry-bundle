This allows you to quickly spin up a Repose container that will proxy traffic from port `8888` to an instance of the Admin API running locally (such as within an IDE / IntelliJ).

It can be used to test Repose config changes while being able to configure breakpoints in the application to see what headers are being passed down.

The `Local Repose` environment in the [Insomnia Workspace](https://github.com/racker/salus-telemetry-bundle/blob/master/dev/Insomnia-workspace.yaml) can be used for these requests.

## Usage

### Start Repose

Before starting the Repose services you will need to declare the Keystone/Identity credentials of a service/user account that is authorized to validate authentication tokens:

```bash
export KEYSTONE_USER=...
export KEYSTONE_PASSWORD=...
```

Start the repose container with the command:
```
docker-compose up -d
```

### Configure the Admin API
Assuming the `dev` profile is being used, the configuration will need to be modified to ensure it includes all authentication filters.  This can be done by removing the following section from the `application-dev.yml`:

```yaml
spring:
  profiles:
    include: "unsecured"
```

The following application property will also need to be updated to override the defaults and be able to authenticate the required Keystone roles.

1. `SALUS_API_PUBLIC_ROLES`
   * [SALUS_API_ADMIN_ROLES](https://github.com/racker/salus-telemetry-api/blob/master/admin/src/main/java/com/rackspace/salus/telemetry/api/config/ApiAdminProperties.java)

The value will vary depending on what is being tested, but to replicate values already running in a Kubernetes environment first ensure `kubectl` is connected to the correct environment and then run:

```
kubectl describe deployment api-admin
```

Locate the `SALUS_API_ADMIN_ROLES` property and set the returned value in the relevant application-dev.yml configs.

### Start Appications
Within IntelliJ all relevant applications can now be started.  Any requests to localhost:8888 should now be proxied through to the local Admin API.

## Stopping the container

```
docker-compose down -v
```