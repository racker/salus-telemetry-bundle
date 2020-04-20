### Running the Docker application images

**NOTE** When running with Docker for Desktop, you might need to increase the VM
settings to 4 vCPUs and 8 GiB of memory. Those settings are located in the "Advanced" tab.

The following environment variables will need to be set when running Docker Compose.  You can set those without the `export` in a file named `dev/telemetry-apps/.env`, set them in IntelliJ's run config, if running Compose there, or set them via your shell.  The `.env` approach is best since it works with all the ways of running Compose.

- `IMG_PREFIX`
- `VAULT_APP_ROLE_ROLE_ID`
- `VAULT_APP_ROLE_SECRET_ID`

For Google Cloud Builder built images, the `IMG_PREFIX` would be something like the following,
where `PROJECT_ID` is replaced with GCP project ID.

    gcr.io/PROJECT_ID/

**NOTE** the trailing slash is *required*. Use `gcloud auth configure-docker`, [described here](https://cloud.google.com/container-registry/docs/pushing-and-pulling), to configure authentication for pulling those images.

The "VAULT" values are provided during the Vault setup in the "Setting up Vault for development usage" in the [main readme](https://github.com/racker/salus-telemetry-bundle/blob/master/README.md).

With those set, you should first pull in the latest images by executing the following in the `dev/telemetry-apps` directory:

```bash
docker-compose pull
```

If you already had older images in `docker images` for these application, it is a good idea to remove those.

Next, run:

```bash
docker-compose up -d
```

Running `docker-compose events` afterwards can help you see what is going on and if any failure occur.  Occasionally kafka can fail to start which leads to app failures.

The Envoy config file `dev/envoy-config-authserv.yml` can be used with this set of containers.

The `dev/telemetry-apps` also contains a `check-health.sh` script, which you can run to query the actuator health endpoints of the apps. The built-in [healthcheck](https://docs.docker.com/compose/compose-file/#healthcheck) couldn't be used since it assumes the use of `curl`, but that is purposely not installed
in the app containers.

#### Including Repose to replicate deployment-time authentication

Before starting the Repose services you will need to declare the Keystone/Identity credentials of a service/user account that is authorized to validate authentication tokens:

```bash
export KEYSTONE_USER=...
export KEYSTONE_PASSWORD=...
```

With that, you can run the following in the `dev/telemetry-apps` directory to start the Repose services each for public and admin authentication:

```bash
docker-compose -f docker-compose-repose.yml up -d
```