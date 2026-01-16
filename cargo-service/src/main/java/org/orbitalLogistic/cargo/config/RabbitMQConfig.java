package org.orbitalLogistic.cargo.config;

import org.springframework.amqp.core.Queue;
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
 * RabbitMQ configuration for cargo-service.
 * Subscribes to events from global Topic Exchange using routing keys.
 */
@Configuration
public class RabbitMQConfig {

    @Value("${spring.rabbitmq.host:haproxy}")
    private String host;

    @Value("${spring.rabbitmq.port:5672}")
    private int port;

    @Value("${spring.rabbitmq.username:admin}")
    private String username;

    @Value("${spring.rabbitmq.password:admin}")
    private String password;

    // Queue name for this service
    public static final String QUEUE_NAME = "cargo-service-queue";

    // Routing keys (event names) this service is interested in
    // TODO: Add/remove routing keys based on what events cargo-service needs to handle
    public static final String ROUTING_KEY_MISSION_CREATED = "mission.created";
    public static final String ROUTING_KEY_MISSION_UPDATED = "mission.updated";
    public static final String ROUTING_KEY_CARGO_CREATED = "cargo.created";
    public static final String ROUTING_KEY_CARGO_UPDATED = "cargo.updated";

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

    /**
     * Creates Quorum Queue for this service.
     */
    @Bean
    public Queue queue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-queue-type", "quorum");
        return new Queue(QUEUE_NAME, true, false, false, args);
    }
}

