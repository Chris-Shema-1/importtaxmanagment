package com.importtax.server.broker;

import com.importtax.server.constants.NotificationType;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Embedded ActiveMQ broker for OTP and business notification events.
 * OTP uses {@value #OTP_QUEUE_NAME}; payment and clearance use {@value #BUSINESS_QUEUE_NAME}.
 */
public final class NotificationBroker {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationBroker.class);
    private static final String BROKER_URL = "vm://localhost?broker.persistent=false";
    private static final String OTP_QUEUE_NAME = "OTP.NOTIFICATIONS";
    private static final String BUSINESS_QUEUE_NAME = "BUSINESS.NOTIFICATIONS";
    private static final DateTimeFormatter TIMESTAMP_FMT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    private static final Set<String> PROCESSED_MESSAGE_IDS = ConcurrentHashMap.newKeySet();

    private static BrokerService broker;
    private static ConnectionFactory factory;

    private NotificationBroker() {}

    /** Start the embedded broker and wire up OTP and business consumers. */
    public static synchronized void start() {
        if (broker != null) {
            return;
        }
        try {
            broker = new BrokerService();
            broker.setBrokerName("importtax-broker");
            broker.setPersistent(false);
            broker.setUseJmx(false);
            broker.start();

            factory = new ActiveMQConnectionFactory(BROKER_URL);
            startOtpConsumer();
            startBusinessConsumer();
            LOGGER.info("ActiveMQ embedded broker started — queues: {}, {}",
                    OTP_QUEUE_NAME, BUSINESS_QUEUE_NAME);
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
            Destination dest = session.createQueue(OTP_QUEUE_NAME);
            MessageProducer producer = session.createProducer(dest);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage msg = session.createTextMessage();
            msg.setStringProperty("eventType", NotificationType.OTP);
            msg.setStringProperty("recipient", recipient);
            msg.setStringProperty("otp", otp);
            msg.setText("OTP for " + recipient + ": " + otp);
            producer.send(msg);
            LOGGER.info("OTP event published for recipient={}", recipient);
        } catch (Exception e) {
            LOGGER.error("Failed to publish OTP event", e);
        }
    }

    /** Publish a payment-confirmed business event. */
    public static void publishPaymentNotification(
            String invoiceNumber,
            String itemName,
            BigDecimal amount,
            String username,
            String recipientEmail,
            LocalDateTime timestamp,
            String notificationType,
            String title,
            String message) {
        publishBusinessEvent(
                notificationType,
                title,
                message,
                username,
                recipientEmail,
                timestamp,
                msg -> {
                    msg.setStringProperty("invoiceNumber", invoiceNumber);
                    msg.setStringProperty("itemName", itemName);
                    if (amount != null) {
                        msg.setStringProperty("amountPaid", amount.toPlainString());
                    }
                });
    }

    /** Publish an import-cleared business event. */
    public static void publishClearanceNotification(
            String itemName,
            String importerName,
            LocalDate clearanceDate,
            String invoiceReference,
            String username,
            String recipientEmail,
            LocalDateTime timestamp,
            String notificationType,
            String title,
            String message) {
        publishBusinessEvent(
                notificationType,
                title,
                message,
                username,
                recipientEmail,
                timestamp,
                msg -> {
                    msg.setStringProperty("itemName", itemName);
                    msg.setStringProperty("importerName", importerName);
                    if (clearanceDate != null) {
                        msg.setStringProperty("clearanceDate", clearanceDate.toString());
                    }
                    msg.setStringProperty("invoiceReference", invoiceReference);
                });
    }

    private static void publishBusinessEvent(
            String notificationType,
            String title,
            String message,
            String username,
            String recipientEmail,
            LocalDateTime timestamp,
            PropertySetter extraProperties) {
        if (factory == null) {
            LOGGER.warn("Broker not started — business event not published type={}", notificationType);
            return;
        }
        try (Connection conn = factory.createConnection();
             Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE)) {
            conn.start();
            Destination dest = session.createQueue(BUSINESS_QUEUE_NAME);
            MessageProducer producer = session.createProducer(dest);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
            TextMessage msg = session.createTextMessage();
            msg.setStringProperty("eventType", notificationType);
            msg.setStringProperty("notificationType", notificationType);
            msg.setStringProperty("title", title);
            msg.setStringProperty("username", username);
            msg.setStringProperty("recipient", username);
            msg.setStringProperty("recipientEmail", recipientEmail != null ? recipientEmail : "");
            msg.setStringProperty("timestamp", timestamp != null ? timestamp.format(TIMESTAMP_FMT) : "");
            extraProperties.apply(msg);
            msg.setText(message);
            producer.send(msg);
            LOGGER.info("Business event published type={} recipient={}", notificationType, username);
        } catch (Exception e) {
            LOGGER.error("Failed to publish business event type={}", notificationType, e);
        }
    }

    /** OTP consumer — simulates email delivery by logging the OTP. */
    private static void startOtpConsumer() throws Exception {
        Connection conn = factory.createConnection();
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest = session.createQueue(OTP_QUEUE_NAME);
        MessageConsumer consumer = session.createConsumer(dest);
        consumer.setMessageListener(message -> handleOtpMessage(message));
        conn.start();
    }

    /** Business consumer — logs payment and clearance events (email simulation). */
    private static void startBusinessConsumer() throws Exception {
        Connection conn = factory.createConnection();
        Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination dest = session.createQueue(BUSINESS_QUEUE_NAME);
        MessageConsumer consumer = session.createConsumer(dest);
        consumer.setMessageListener(message -> handleBusinessMessage(message));
        conn.start();
    }

    private static void handleOtpMessage(Message message) {
        try {
            if (!(message instanceof TextMessage tm)) {
                return;
            }
            if (!markProcessed(message)) {
                LOGGER.debug("Skipping duplicate OTP message id={}", message.getJMSMessageID());
                return;
            }
            String recipient = tm.getStringProperty("recipient");
            String otp = tm.getStringProperty("otp");
            LOGGER.info("╔══════════════════════════════════════╗");
            LOGGER.info("║  [EMAIL SIMULATION] OTP              ║");
            LOGGER.info("║  To      : {}", recipient);
            LOGGER.info("║  Subject : Your Import Tax OTP Code  ║");
            LOGGER.info("║  OTP     : {}                        ║", otp);
            LOGGER.info("║  Valid for 5 minutes                 ║");
            LOGGER.info("╚══════════════════════════════════════╝");
            LOGGER.info("OTP event received for recipient={}", recipient);
        } catch (Exception e) {
            LOGGER.error("OTP consumer error", e);
        }
    }

    private static void handleBusinessMessage(Message message) {
        try {
            if (!(message instanceof TextMessage tm)) {
                return;
            }
            if (!markProcessed(message)) {
                LOGGER.debug("Skipping duplicate business message id={}", message.getJMSMessageID());
                return;
            }
            String eventType = tm.getStringProperty("eventType");
            String username = tm.getStringProperty("username");
            String title = tm.getStringProperty("title");
            LOGGER.info("Business event received type={} recipient={}", eventType, username);
            LOGGER.info("╔══════════════════════════════════════╗");
            LOGGER.info("║  [EMAIL SIMULATION] {}  ║", padType(eventType));
            LOGGER.info("║  To      : {}", username);
            LOGGER.info("║  Subject : {}  ║", truncate(title, 28));
            LOGGER.info("║  Body    : (see message text)        ║");
            LOGGER.info("╚══════════════════════════════════════╝");
            LOGGER.info("Business notification processed type={}", eventType);
        } catch (Exception e) {
            LOGGER.error("Business consumer error", e);
        }
    }

    private static boolean markProcessed(Message message) throws JMSException {
        String id = message.getJMSMessageID();
        if (id == null) {
            return true;
        }
        return PROCESSED_MESSAGE_IDS.add(id);
    }

    private static String padType(String type) {
        if (type == null) {
            return "BUSINESS          ";
        }
        return type.length() >= 18 ? type.substring(0, 18) : String.format("%-18s", type);
    }

    private static String truncate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max - 3) + "...";
    }

    public static synchronized void stop() {
        PROCESSED_MESSAGE_IDS.clear();
        if (broker != null) {
            try {
                broker.stop();
            } catch (Exception e) {
                LOGGER.warn("Broker stop error", e);
            }
            broker = null;
            factory = null;
        }
    }

    @FunctionalInterface
    private interface PropertySetter {
        void apply(TextMessage message) throws JMSException;
    }
}
