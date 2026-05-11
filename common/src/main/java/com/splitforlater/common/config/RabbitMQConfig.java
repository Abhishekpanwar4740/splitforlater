package com.splitforlater.common.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RabbitMQConfig {
    public static final String EXPENSE_QUEUE_NAME = "expenses.queue";
    public static final String EXPENSE_EXCHANGE_NAME = "expense.exchange";
    public static final String EXPENSE_ROUTING_KEY = "expense.routing.key";

    public static final String SETTLEMENT_QUEUE_NAME = "settlement.queue";
    public static final String SETTLEMENT_EXCHANGE_NAME = "settlement.exchange";
    public static final String SETTLEMENT_ROUTING_KEY = "settlement.routing.key";
    @Bean
    public Queue settlementQueue() {
        return new Queue(SETTLEMENT_QUEUE_NAME, true); // true = durable
    }

    @Bean
    public DirectExchange settlementExchange() {
        return new DirectExchange(SETTLEMENT_EXCHANGE_NAME);
    }

    @Bean
    public Binding settlementBinding(Queue settlementQueue, DirectExchange settlementExchange) {
        return BindingBuilder.bind(settlementQueue)
                .to(settlementExchange)
                .with(SETTLEMENT_ROUTING_KEY);
    }

    @Bean
    public Queue expenseQueue() {
        return new Queue(EXPENSE_QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange expenseExchange() {
        return new DirectExchange(EXPENSE_EXCHANGE_NAME);
    }

    @Bean
    public Binding expenseBinding(Queue expenseQueue, DirectExchange expenseExchange) {
        return BindingBuilder.bind(expenseQueue)
                .to(expenseExchange)
                .with(EXPENSE_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}
