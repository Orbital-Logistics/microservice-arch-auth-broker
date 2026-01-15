# RabbitMQ Cluster Configuration

## Как работает автоматический кластер

В `docker-compose.yaml` настроен кластер RabbitMQ из 3 нод, который **автоматически** формируется при `docker compose up`:

1. **rabbitmq1** - главный узел кластера (запускается первым)
2. **rabbitmq2** - автоматически присоединяется к кластеру через встроенные команды
3. **rabbitmq3** - автоматически присоединяется к кластеру через встроенные команды

## Где настроено присоединение к кластеру

### 1. Общие настройки (все ноды):
- `RABBITMQ_ERLANG_COOKIE: "SWQOKODSQALRPCLNMEQG"` - одинаковый cookie для всех нод (обязательно!)
- `RABBITMQ_USE_LONGNAME: "true"` - использование полных имен узлов
- `RABBITMQ_NODENAME: "rabbit@rabbitmq1/2/3"` - уникальное имя каждой ноды

### 2. Автоматическое присоединение rabbitmq2 к кластеру:

В `docker-compose.yaml` для `rabbitmq2` в секции `command`:
```yaml
command: >
  bash -c "
    docker-entrypoint.sh rabbitmq-server -detached &&
    sleep 15 &&
    until rabbitmqctl -n rabbit@rabbitmq1 ping > /dev/null 2>&1; do
      echo 'Waiting for rabbitmq1...';
      sleep 2;
    done &&
    rabbitmqctl stop_app &&
    rabbitmqctl join_cluster rabbit@rabbitmq1 &&
    rabbitmqctl start_app &&
    rabbitmqctl stop &&
    exec docker-entrypoint.sh rabbitmq-server
  "
```

**Что происходит:**
1. Запускается RabbitMQ в фоновом режиме
2. Ожидается готовность rabbitmq1
3. Останавливается приложение RabbitMQ
4. Выполняется присоединение к кластеру: `rabbitmqctl join_cluster rabbit@rabbitmq1`
5. Запускается приложение RabbitMQ
6. Перезапускается сервер в обычном режиме

### 3. Автоматическое присоединение rabbitmq3 к кластеру:

Аналогично для `rabbitmq3`, но ожидает готовности и rabbitmq1, и rabbitmq2.

## Проверка кластера

После запуска всех контейнеров можно проверить кластер:

```bash
# Проверить статус кластера на любой ноде
docker exec rabbitmq1 rabbitmqctl cluster_status

# Или через Management UI
# http://localhost:15672 (admin/admin)
```

## Важно

- Все ноды должны иметь **одинаковый** `RABBITMQ_ERLANG_COOKIE`
- Ноды присоединяются к кластеру **автоматически** при `docker compose up`
- Quorum Queues автоматически реплицируются на все ноды кластера
- **Никаких локальных файлов не требуется** - все настраивается через docker-compose

