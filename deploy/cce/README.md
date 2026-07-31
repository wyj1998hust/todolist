# CCE 内网部署

所有资源部署到 `todo-internal` 命名空间。前端和后端均为 `ClusterIP` Service，只有 `internal-nginx` Ingress Class 对接的内网 ELB 可以访问它们。

部署前替换：

- `swr.example.com/todo/*:REPLACE_TAG` 为实际 SWR 镜像地址与版本。
- `tdsql.internal.example.com`、`todo_app` 为实际 TDSQL 连接配置。
- `todo.intra.example.com` 为企业内网 DNS 名称。
- `internal-nginx` 为集群中绑定内网 ELB、且不分配公网 IP 的 Ingress Class。

部署顺序：

```powershell
kubectl apply -f namespace.yaml
kubectl apply -f configmap.yaml
kubectl apply -f secret.yaml
kubectl apply -f migration-job.yaml
kubectl -n todo-internal wait --for=condition=complete job/todo-migration --timeout=5m
kubectl apply -f backend.yaml
kubectl apply -f frontend.yaml
kubectl apply -f ingress-internal.yaml
```

若内网入口使用 HTTPS，设置 `APP_AUTH_SECURE_COOKIE=true`，并在 Ingress 中配置内部 TLS 证书。
