package org.orbitalLogistic.messageproducer.config;

import com.rabbitmq.client.AMQP;
import org.springframework.amqp.core.*;
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

    @Value("${spring.rabbitmq.host:haproxy}")
    private String host;

    @Value("${spring.rabbitmq.port:5672}")
    private int port;

    @Value("${spring.rabbitmq.username:admin}")
    private String username;

    @Value("${spring.rabbitmq.password:admin}")
    private String password;

    public static final String TOPIC_EXCHANGE_NAME = "global-exchange";

    // Routing keys
    // Format: <domain>.<event>
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

    @Bean
    public TopicExchange topicExchange() {
        return new TopicExchange(TOPIC_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue globalQueue() {
        return QueueBuilder.durable("global-queue")
                .quorum()
                .build();
    }

    // ADD MORE QUEUES FOR DIFFERENT EVENTS AND LISTENERS

    @Bean
    public Binding bindingMissionCreated(Queue globalQueue, TopicExchange topicExchange) {
        return BindingBuilder.bind(globalQueue)
                .to(topicExchange)
                .with("YOUR_ROUTING_KEY");
    }

    // BIND NEW QUEUES LIKE PREVIOUS EXAMPLE
}
