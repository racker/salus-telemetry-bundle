The standard practice for our Spring Boot applications is to run them in our development
workspace with the Spring profile "dev" activated by setting the `spring.profiles.active`
(as described [here](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-profiles.html#boot-features-adding-active-profiles)).

As such, another standard practice is to declare an `application-dev.yml` file in the respective
`src/main/resources` of the application. Since most of our web applications bind an HTTP port
at least for actuator health access, the `server.port` has been declared in each of those
`-dev` files to be a distinct port. This allows us to run any number of our applications
concurrently to perform integration.

The following table lists and needs to be maintained with the port values assigned to each
application. _When creating a new application be sure to assign `server.port` accordingly and
update the table._

Port | Application / Usage
-----|---------------------
8080 | Public API
8081 | Ambassador
8082 | Auth Service
8083 | Presence Monitor
8084 | Zone Management
8085 | Resource Management
8086 | _Reserved for local InfluxDB usage_
8087 | Event Engine Management
8088 | _Unused_
8089 | Monitor Management
8433 | Admin API when running with `ssl` profile activated
8888 | Admin API
