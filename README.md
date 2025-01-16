## Реактивный сервис хранения файлов и получения видеопотока

### Сборка запуск только контейнера с сервисом:
```
docker build -f ./devops/app.Dockerfile -t reactive-video:1.0 .
docker run --name reactive-video -p 8081:8081 -e MONGODB_URI=mongodb://host.docker.internal:27017/files -e S3_HOST=http://host.docker.internal:9000 -e S3_ACCESS_KEY=enfb53u7gPFGhmgYGXgw -e S3_SECRET_KEY=xP9kqRZ5QmQi4LrF1cQqHnFHa56eW3V3Bf3jvZyn -e FILE_ASYNC_LOAD_CHUNK_SIZE="#{1024 * 1024}" reactive-video:1.0
```

### Запуск сервиса и его инфраструктуры в docker compose:
```
docker compose -f devops/docker-compose.yaml -p reactive-video up -d
```

### Запуск сервиса и его инфраструктуры в kubernetes:
```
kubectl apply -f devops/k8s/reactive-video-secret.yaml
kubectl apply -f devops/k8s/reactive-video-envs.yaml
kubectl apply -f devops/k8s/reactive-video-deployment.yaml
```

Для тестирования работы сервиса можно воспользоваться коллекцией Postman и файлами с примерами в директории /etc