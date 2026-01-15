# Инструкция по обновлению RabbitMQ конфигурации

Все бизнес-сервисы обновлены для работы с глобальным Topic Exchange.

## Что изменилось:

1. **Producer-сервис**: Теперь использует один Topic Exchange `events-exchange` и публикует события по routing keys
2. **Бизнес-сервисы**: Подписываются на нужные routing keys через bindings к Topic Exchange

## Как это работает:

- Producer отправляет событие: `publishEvent("mission.created", eventData)`
- Событие попадает в Topic Exchange с routing key `mission.created`
- Все сервисы, которые подписаны на `mission.created`, получают это событие
- Producer НЕ знает, какие сервисы слушают события

## Для добавления нового события:

1. Добавьте routing key в `RabbitMQConfig` producer-сервиса
2. В сервисах, которые должны получать это событие, добавьте Binding в их `RabbitMQConfig`

