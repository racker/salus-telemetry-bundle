This composition starts a Prometheus instance that can be configured to monitor a locally deployed etcd from [the infra composition](../telemetry-infra) (the default) or a port-forwarded etcd instance such as from the perf or dev cluster. 

A port-forwarding to a Kubernetes deployed instance can be created with:

```
kubectl port-forward pod/etcd-0 32379:2379
```

You will need to uncomment the documented section in [prometheus.yml](prometheus.yml) to configure a scrape of the port-forwarded instance.

The Prometheus dashboard is accessible at http://localhost:9090/. Specifically, the http://localhost:9090/targets view can be used to confirm the scraping of the etcd instance(s).

The Grafana dashboard is available at http://localhost:3000 where the initial credentials are admin/admin. [Dashboard ID 3070](https://grafana.com/grafana/dashboards/3070) can be imported at http://localhost:3000/dashboard/import to setup etcd visibility.