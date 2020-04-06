This composition tests out the ability to use Repose to whitelist only a specific path, `/v1.0/cert`, that be passed through to the auth service. All the other paths of that service are meant to be proxied via the public API (and Identity/Keystone authentication in Repose).

> NOTE: there might be a better way to do the blackhole mechanism, but current config gets the job done

## Usage

Start the composition with

    docker-compose up
    
Start `auth-service` with the `dev` and `devtoken` Spring profiles activated.

Run the HTTP requests in [testing.http](testing.http) with IntelliJ.