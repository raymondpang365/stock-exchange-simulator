package com.raymondpang365.utility;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

import static javax.jms.Session.AUTO_ACKNOWLEDGE;

public final class ExchangeSimulatorMessageConsumer implements ExchangeSimulatorLifeCycle {

    private final Connection connection;
    private final Session session;
    private final MessageConsumer consumer;

    public ExchangeSimulatorMessageConsumer(final String brokerUrl,
                                            final String destinationName,
                                            final DestinationType destinationType,
                                            final MessageListener messageListener,
                                            final String clientIDSuffix,
                                            final String messageSelector,
                                            final ExceptionListener exceptionListener) throws JMSException {
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connectionFactory.setTrustAllPackages(true);

        connection = connectionFactory.createConnection();
        connection.setClientID(String.format("%sConsumer_%s", destinationName, clientIDSuffix));
        session = connection.createSession(false, AUTO_ACKNOWLEDGE);
        consumer = session.createConsumer(destinationType == DestinationType.Queue ?
                                                  session.createQueue(destinationName)
                                                  : session.createTopic(destinationName), messageSelector);
        if (messageListener != null) {
            consumer.setMessageListener(messageListener);
        }
        if (exceptionListener != null) {
            connection.setExceptionListener(exceptionListener);
        }
    }

    @Override
    public void start() throws Exception {
        connection.start();
    }

    @Override
    public void stop() throws Exception {
        connection.close();
    }
}
