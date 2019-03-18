This application consumes event JSON messages from Kafka that were originated by the
[Kafka event handler in Kapacitor](https://docs.influxdata.com/kapacitor/v1.5/event_handlers/kafka/)
and writes those events as Influx line protocol to a configured InfluxDB instance.

The following diagram shows where this tool fits into the Kapacitor-Kafka-InfluxDB pipeline:

![](docs/kapacitor-kafka-ingest.drawio.png)

Typically, this tool will be running in conjunction with the Kafka instance provided by the
Docker composition in `dev/telemetry-infra`.

The following is example JSON content produced by the Kapacitor Kakfa event handler:

```json
{
  "id": "tenant-00:resource-002:cpu:used",
  "message": "tenant-00:resource-002:cpu:used is CRITICAL",
  "details": "task=tenant-00-cpu-2517b19e-b334-4221-bb40-1ad9754edef1",
  "time": "2019-02-13T22:02:28.352Z",
  "duration": 0,
  "level": "CRITICAL",
  "data": {
    "series": [
      {
        "name": "cpu",
        "tags": {
          "account": "tenant-00",
          "accountType": "UNKNOWN",
          "arch": "x64",
          "env": "stage",
          "monitoringSystem": "SALUS",
          "os": "linux",
          "resourceId": "resource-002"
        },
        "columns": [
          "time",
          "total",
          "used"
        ],
        "values": [
          [
            "2019-02-13T22:02:28.352Z",
            23,
            103
          ]
        ]
      }
    ]
  },
  "previousLevel": "OK",
  "recoverable": true
}
```

## Grafana Dashboards

The ingested events can be viewed in Grafana along with the raw metrics by using the following two dashboards, 
where the second annotates the metrics chart with the events:
- [Overview Dashboard JSON](grafana-dashboards/Overview.json)
- [Detailed Dashboard JSON](grafana-dashboards/Detailed.json)