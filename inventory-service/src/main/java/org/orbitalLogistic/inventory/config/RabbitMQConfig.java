package org.orbitalLogistic.inventory.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RabbitMQ configuration for inventory-service.
 * Subscribes to events from global Topic Exchange using routing keys.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host:rabbitmq1}")
    private String host;

    @Value("${spring.rabbitmq.port:5672}")
    private int port;

    @Value("${spring.rabbitmq.username:admin}")
    private String username;

    @Value("${spring.rabbitmq.password:admin}")
    private String password;

    // Global Topic Exchange (same as in producer-service)
    public static final String TOPIC_EXCHANGE_NAME = "events-exchange";

    // Queue name for this service
    public static final String QUEUE_NAME = "inventory-service-queue";

    // Routing keys (event names) this service is interested in
    // TODO: Add/remove routing keys based on what events inventory-service needs to handle
    public static final String ROUTING_KEY_CARGO_CREATED = "cargo.created";
    public static final String ROUTING_KEY_CARGO_UPDATED = "cargo.updated";
    public static final String ROUTING_KEY_MISSION_CREATED = "mission.created";
    public static final String ROUTING_KEY_MISSION_UPDATED = "mission.updated";
    public static final String ROUTING_KEY_INVENTORY_TRANSACTION = "inventory.transaction";

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }

    @Bean
    public Queue queue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-queue-type", "quorum");
        return new Queue(QUEUE_NAME, true, false, false, args);
    }

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding bindingCargoCreated(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_CARGO_CREATED);
    }

    @Bean
    public Binding bindingCargoUpdated(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_CARGO_UPDATED);
    }

    @Bean
    public Binding bindingMissionCreated(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_MISSION_CREATED);
    }

    @Bean
    public Binding bindingMissionUpdated(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_MISSION_UPDATED);
    }

    @Bean
    public Binding bindingInventoryTransaction(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(ROUTING_KEY_INVENTORY_TRANSACTION);
    }
}

