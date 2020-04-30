# InfluxDB & Grafana

This can be used for local testing, such as verifying new counter metrics are appearing as expected.

1. Start the two containers using `docker-compose -f docker-compose-influxdb.yml up -d`
1. Browse to http://localhost:3000/datasources which should open up the data sources view in Grafana
1. Select InfluxDB (or create it if one doesn't exist)
1. Specify `http://influxdb:8086` for the URL
1. Test & Save the configuration

You should now be able to create & edit dashboards to contain your new metrics.
