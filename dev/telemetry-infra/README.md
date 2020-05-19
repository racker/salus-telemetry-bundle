# InfluxDB & Grafana

This can be used for local testing, such as verifying new counter metrics are appearing as expected.

1. Start the two containers using `docker-compose -f docker-compose-influxdb.yml up -d`
1. Access Grafana at http://localhost:3000
   > The default credentials are admin / admin . When prompted to change the password, you can always "change" it to "admin" for simplicity
1. The InfluxDB data source is pre-configured by grafana, so you should now be able to create & edit dashboards to contain your new metrics.
