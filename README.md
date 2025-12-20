## Реактивный сервис хранения файлов и получения видеопотока

Небольшой проект, который был выполнен в рамках изучения тем в ИПР.  
Проект представляет собой сервис по загрузке и скачиванию файлов. 
Сервис асинхронный и построен на Spring WebFlux.  
Контроллеры сервиса предоставляют методы блокирующего и неблокирующего получения потока содержимого файла. 
В случае, если загруженный файл - видеофайл расширения .mp4, то есть возможность получения видеопотока содержимого этого файла.  
Основной сервис системы, который выполняет бОльшую часть работы - FileService.  
Для хранения метаданных файлов используется MongoDB, сами же файлы хранятся в S3 контейнере MinIO.  
За взаимодействие с S3 хранилищем отвечает репозиторий S3FileRepository.  
Добавлена интеграция с VK Cloud: VK Oauth, VK Vision - для распознавания объектов на изображении, VK Voice - для озвучивания текстовых файлов.  
В директории frontend/ расположено React фронтенд приложение для сервиса.  
Часть кода покрыта тестами, среди них: юнит-тесты асинхронного кода с валидацией элементов реактивного потока, юнит-тесты синхронного кода, интеграционные тесты с использованием тест контейнеров.  
Для развертывания приложения в контейнерах были написаны Dockerfile и docker-compose.yaml. 
Также были написаны манифесты развёртывания сервиса для Kubernetes. Devops конфиги расположены в директории /devops.

### Сборка backend и frontend:
```
docker build -f ./devops/backend.Dockerfile -t reactive-video:1.0 .
docker build -f ./devops/frontend.Dockerfile -t reactive-video-frontend:1.0 .
```

### Запуск контейнеров backend и frontend:
```
docker network create reactive-video-network

docker run \
--name reactive-video-backend \
-p 8081:8081 \
-e MONGODB_URI=mongodb://host.docker.internal:27017/files \
-e S3_HOST=http://host.docker.internal:9000 \
-e S3_ACCESS_KEY=S3_ACCESS_KEY \
-e S3_SECRET_KEY=S3_SECRET_KEY \
-e VK_VISION_OAUTH_CLIENT_ID=VK_VISION_OAUTH_CLIENT_ID \
-e VK_VISION_OAUTH_REFRESH_TOKEN=VK_VISION_OAUTH_REFRESH_TOKEN \
-e VK_VOICE_OAUTH_CLIENT_ID=VK_VOICE_OAUTH_CLIENT_ID \
-e VK_VOICE_OAUTH_REFRESH_TOKEN=VK_VOICE_OAUTH_REFRESH_TOKEN \
--env-file ./devops/backend.env \
--network reactive-video-network \
reactive-video:1.0

docker run \
--name reactive-video-frontend \
-p 8080:80 \
-e BACKEND_HOST=reactive-video-backend:8081 \
--network reactive-video-network \
reactive-video-frontend:1.0
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
