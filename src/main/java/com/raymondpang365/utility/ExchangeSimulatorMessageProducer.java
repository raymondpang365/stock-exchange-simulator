package com.raymondpang365.utility;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.*;

public class ExchangeSimulatorMessageProducer implements ExchangeSimulatorLifeCycle {

    private final Connection connection;
    private final Session session;
    private final MessageProducer producer;

    public ExchangeSimulatorMessageProducer(final String brokerUrl,
                                            final String destinationName,
                                            final DestinationType destinationType,
                                            final String clientIDSuffix,
                                            final ExceptionListener exceptionListener) throws JMSException {
        final ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(brokerUrl);
        connectionFactory.setTrustAllPackages(true);

        connection = connectionFactory.createConnection();
        connection.setClientID(String.format("%sProducer_%s", destinationName, clientIDSuffix));
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        producer = session.createProducer(destinationType == DestinationType.Queue ?
                                                  session.createQueue(destinationName)
                                                  : session.createTopic(destinationName));
        if (exceptionListener != null) {
            connection.setExceptionListener(exceptionListener);
        }
    }

    public Session getSession() {
        return session;
    }

    public MessageProducer getProducer() {
        return producer;
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
