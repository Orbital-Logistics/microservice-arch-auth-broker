package org.orbitalLogistic.messageproducer.config;

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

    public static final String TOPIC_EXCHANGE_NAME = "events-exchange";

    // Example routing keys (event names) - можно добавлять новые
    // Формат: <service>.<action> или <domain>.<event>
    public static final String ROUTING_KEY_MISSION_CREATED = "mission.created";
    public static final String ROUTING_KEY_MISSION_UPDATED = "mission.updated";
    public static final String ROUTING_KEY_CARGO_CREATED = "cargo.created";
    public static final String ROUTING_KEY_CARGO_UPDATED = "cargo.updated";
    public static final String ROUTING_KEY_USER_CREATED = "user.created";
    public static final String ROUTING_KEY_USER_UPDATED = "user.updated";
    public static final String ROUTING_KEY_SPACECRAFT_CREATED = "spacecraft.created";
    public static final String ROUTING_KEY_SPACECRAFT_UPDATED = "spacecraft.updated";
    public static final String ROUTING_KEY_INVENTORY_TRANSACTION = "inventory.transaction";
    public static final String ROUTING_KEY_MAINTENANCE_LOG = "maintenance.log";

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
     * Creates Topic Exchange for pub/sub pattern.
     * Services subscribe to routing keys (event names) they are interested in.
     */
    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE_NAME, true, false);
    }
}

