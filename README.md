## Реактивный сервис хранения файлов и получения видеопотока

Небольшой проект, который был выполнен в рамках изучения тем в ИПР. Проект представляет из себя сервис по загрузке и скачиванию файлов. Сервис асинхронный и построен на Spring WebFlux. Контроллеры сервиса предоставляют методы блокирующего и неблокирующего получения потока содержимого файла. В случае, если загруженный файл - видеофайл расширения .mp4, то есть возможность получения видеопотока содержимого этого файла. Основной сервис системы, который выполняет бОльшую часть работы - FileService. Для хранения метаданных файлов используется MongoDB, сами же файлы хранятся в S3 контейнере MinIO. За взаимодействие с S3 хранилищем отвечает репозиторий S3FileRepository. Часть кода покрыта тестами, среди них: юнит-тесты асинхронного кода с валидацией элементов реактивного потока, юнит-тесты синхронного кода, интеграционные тесты с использованием тест контейнеров. Для развертывания приложения в контейнерах были написаны Dockerfile и docker-compose.yaml. Также были написаны манифесты развёртывания сервиса для Kubernetes. Devops конфиги расположены в директории /devops.

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