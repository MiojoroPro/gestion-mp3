package com.gestionmp3.common.rabbit;

import com.gestionmp3.common.config.AppConfig;
import com.gestionmp3.common.config.RabbitConstants;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * Petite facade autour du client AMQP : ouvre une connexion, declare la
 * topologie (exchange + les trois files durables) et expose le {@link Channel}.
 *
 * <p>Chaque programme du back office cree un {@code RabbitClient}, declare la
 * topologie (idempotent) puis publie ou consomme sur la file qui le concerne.
 */
public class RabbitClient implements AutoCloseable {

    private final Connection connection;
    private final Channel channel;

    public RabbitClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(AppConfig.rabbitHost());
        factory.setPort(AppConfig.rabbitPort());
        factory.setUsername(AppConfig.rabbitUser());
        factory.setPassword(AppConfig.rabbitPassword());
        factory.setAutomaticRecoveryEnabled(true);

        this.connection = factory.newConnection();
        this.channel = connection.createChannel();
        declareTopology();
    }

    /** Declare l'exchange et les files. Operation idempotente cote RabbitMQ. */
    private void declareTopology() throws IOException {
        channel.exchangeDeclare(RabbitConstants.EXCHANGE, BuiltinExchangeType.DIRECT, true);

        declareAndBind(RabbitConstants.QUEUE_DISCOVERED, RabbitConstants.ROUTING_DISCOVERED);
        declareAndBind(RabbitConstants.QUEUE_METADATA, RabbitConstants.ROUTING_METADATA);
        declareAndBind(RabbitConstants.QUEUE_SENT, RabbitConstants.ROUTING_SENT);
    }

    private void declareAndBind(String queue, String routingKey) throws IOException {
        channel.queueDeclare(queue, true, false, false, null);
        channel.queueBind(queue, RabbitConstants.EXCHANGE, routingKey);
    }

    /** Publie un message persistant sur l'exchange avec la cle de routage donnee. */
    public void publish(String routingKey, byte[] body) throws IOException {
        channel.basicPublish(
                RabbitConstants.EXCHANGE,
                routingKey,
                com.rabbitmq.client.MessageProperties.PERSISTENT_TEXT_PLAIN,
                body);
    }

    public Channel channel() {
        return channel;
    }

    @Override
    public void close() throws IOException, TimeoutException {
        if (channel != null && channel.isOpen()) {
            channel.close();
        }
        if (connection != null && connection.isOpen()) {
            connection.close();
        }
    }
}
