package com.importtax.server.broker;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Embedded ActiveMQ broker for OTP notification events.
 * Starts a broker on vm://localhost, publishes OTP messages to the
 * "OTP.NOTIFICATIONS" queue, and runs a consumer that simulates email delivery.
 */
public final class NotificationBroker {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationBroker.class);
    private static final String BROKER_URL = "vm://localhost?broker.persistent=false";
    private static final String QUEUE_NAME = "OTP.NOTIFICATIONS";

    private static BrokerService broker;
    private static ConnectionFactory factory;

    private NotificationBroker() {}

    /** Start the embedded broker and wire up the OTP consumer. */
    public static synchronized void start() {
        if (broker != null) return;
        try {
            broker = new BrokerService();
            broker.setBrokerName("importtax-broker");
            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.start();

            factory = new ActiveMQConnectionFactory(BROKER_URL);
            startOtpConsumer();
            LOGGER.info("ActiveMQ embedded broker started — queue: {}", QUEUE_NAME);
        } catch (Exception e) {
            LOGGER.error("Failed to start ActiveMQ broker", e);
        }
    }

    /** Publish an OTP event to the queue. */
    public static void publishOtp(String recipient, String otp) {
        if (factory == null) {
            LOGGER.warn("Broker not started — OTP not published for {}", recipient);
            return;
        }
        try (Connection conn = factory.createConnection();
             Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            conn.start();
            Destination dest = session.createQueue(QUEUE_NAME);
            MessageProducer producer = session.createProducer(dest);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage msg = session.createTextMessage();
            msg.setStringProperty("recipient", recipient);
            msg.setStringProperty("otp", otp);
            msg.setText("OTP for " + recipient + ": " + otp);
            producer.send(msg);
            LOGGER.info("OTP event published for recipient={}", recipient);
        } catch (Exception e) {
            LOGGER.error("Failed to publish OTP event", e);
        }
    }

    /** Consumer thread — simulates email delivery by logging the OTP. */
    private static void startOtpConsumer() throws Exception {
        Connection conn = factory.createConnection();
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest = session.createQueue(QUEUE_NAME);
        MessageConsumer consumer = session.createConsumer(dest);
        consumer.setMessageListener(new MessageListener() {
            @Override
            public void onMessage(Message message) {
                try {
                    if (message instanceof TextMessage tm) {
                        String recipient = tm.getStringProperty("recipient");
                        String otp       = tm.getStringProperty("otp");
                        LOGGER.info("╔══════════════════════════════════════╗");
                        LOGGER.info("║  [EMAIL SIMULATION]                  ║");
                        LOGGER.info("║  To      : {}", recipient);
                        LOGGER.info("║  Subject : Your Import Tax OTP Code  ║");
                        LOGGER.info("║  OTP     : {}                        ║", otp);
                        LOGGER.info("║  Valid for 5 minutes                 ║");
                        LOGGER.info("╚══════════════════════════════════════╝");
                    }
                } catch (Exception e) {
                    LOGGER.error("OTP consumer error", e);
                }
            }
        });
        conn.start();
    }

    public static synchronized void stop() {
        if (broker != null) {
            try { broker.stop(); } catch (Exception e) { LOGGER.warn("Broker stop error", e); }
            broker = null;
        }
    }
}
