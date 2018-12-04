package oracle.communications.ordermanagement.util.jms;

import java.io.Serializable;
import java.util.Properties;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.TopicPublisher;
import javax.naming.InitialContext;

import oracle.communications.ordermanagement.util.ThreadLocalMap;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;

import weblogic.jms.extensions.WLMessageProducer;

import com.mslv.oms.util.xml.XMLHelper;

/**
 * 
 * The JMSHelper class provides synchronous JMS messaging functionality. Instances of this class are not Thread-safe.
 */
public final class JMSHelper {

    private static final Log log = LogFactory.getLog(JMSHelper.class);

    private static final int DEFAULT_TIMEOUT = 15000;

    private static final String DEFAULT_QUEUE_FACTORY_JNDI = "javax.jms.QueueConnectionFactory";

    public static final int DEFAULT_PRIORITY = 6;

    private String destination;

    private String factoryName;

    private boolean initialized = false;

    private ConnectionFactory factory;

    private Connection connection;

    private Session session;

    private MessageProducer producer;

    private MessageConsumer consumer;

    private Destination reqDest;

    private Destination respDest;

    private long creationTime = 0;

    /**
     * 
     * Close up all resource.
     * 
     * 
     * 
     * @throws Throwable
     * 
     *             if an underlying exception occurs.
     */
    @Override
    protected void finalize() throws Throwable {

        close();

        super.finalize();

    };

    /**
     * 
     * Initial the underlying JMS connection.
     */
    private void initialize() {
        if (this.initialized) {
            close();
        }

        InitialContext initialcontext = null;
        try {
        	
            initialcontext = new InitialContext();
            this.reqDest = (Destination) initialcontext.lookup(this.destination);
            if (this.factoryName == null) {
                this.factoryName = DEFAULT_QUEUE_FACTORY_JNDI;
            }

            this.factory = (ConnectionFactory) initialcontext.lookup(getFactoryName());
            this.connection = this.factory.createConnection();
            this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            this.producer = this.session.createProducer(this.reqDest);
            this.connection.start();
            initialcontext.close();

            this.initialized = true;

        } catch (final Exception exception) {
            throw new RuntimeException("Failed to initialize JMS connection", exception);
        } finally {
            if (initialcontext != null) {
                try {
                    initialcontext.close();
                } catch (final Throwable eatMe) {
                    log.warn(eatMe);
                }
            }
        }
    }

    /**
     * Contructs a new instance using the passed JMS connection factory and destination JNDI names. If <code>factory</code> is
     * <code>null</code>, the default connection factory ( <code>javax.jms.QueueConnectionFactory</code> or
     * <code>javax.jms.TopicConnectionFactory</code>) is used, based on <code>destination</code>'s type.
     * <p>
     * 
     * <i>Note that as of WLS 7.x, all JMS Destinations implement <b>both </b> </i> <code>javax.jms.Queue</code> <i>and </i>
     * <code>javax.jms.Topic</code> <i>. This implementation calls </i> <code>instanceof Queue</code> <i>on the destination first,
     * guaranteeing that for this release <code>destination</code> will always be considered a <code>Queue</code> </i>.
     * 
     * 
     * 
     * @param factoryName
     *            the connection factory JNDI
     * 
     * @param destination
     *            the JMS destination JNDI.
     */
    private JMSHelper(final String factoryName, final String destination) {

        setFactoryName(factoryName);
        setDestination(destination);
        initialize();
    }

    /**
     * 
     * @return the response Destination object.
     * 
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */
    private Destination getResponseDestination() throws JMSException {

        if (this.respDest == null) {

            this.respDest = this.session.createTemporaryQueue();

            this.consumer = this.session.createConsumer(this.respDest);

        }

        return this.respDest;

    }

    /**
     * 
     * Creates a new <code>javax.jms.TextMessage</code> with the following JMS properties.
     * 
     * <ul>
     * 
     * <li>JMSDeliveryMode= <code>DeliveryMode.NON_PERSISTENT</code></li>
     * 
     * <li>JMSPriority=6</li>
     * 
     * <li>JMSReplyTo= <i>the supplied destination </i></li>
     * 
     * </ul>
     * 
     * and the following message properties:
     * 
     * <ul>
     * 
     * <li>MESSAGE_TYPE=REQUEST</li>
     * 
     * <li>REPLYTO_DESTINATION_TYPE=QUEUE | TOPIC</li>
     * 
     * <li>APPLICATION_TYPE=OMS_VF</li>
     * 
     * <li>APPLICATION_ID=OMS</li>
     * 
     * <li>VERSION_ID=1</li>
     * 
     * </ul>
     * 
     * 
     * 
     * @param replyTo
     * 
     *            The reply to Destination.
     * 
     * @return the newly-created<code> TextMessage</code>
     * 
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */
    public TextMessage createTextMessage(final Destination replyTo) throws JMSException {
        checkInitialized();

        final TextMessage textmessage = this.session.createTextMessage();
        textmessage.setJMSDeliveryMode(DeliveryMode.NON_PERSISTENT);
        textmessage.setJMSPriority(DEFAULT_PRIORITY);

        if (replyTo != null) {
            textmessage.setJMSReplyTo(replyTo);
        }

        textmessage.setStringProperty("MESSAGE_TYPE", "REQUEST");
        textmessage.setStringProperty("APPLICATION_TYPE", "OSM");
        textmessage.setStringProperty("APPLICATION_ID", "OSM");
        textmessage.setIntProperty("VERSION_ID", 1);

        return textmessage;

    }

    /**
     * Creates object message to be populated by the caller.
     * 
     * @return newly-created <code>ObjectMessage</code>
     * 
     * @throws JMSException
     *             if an underlying JMSException occurs.
     */
    public ObjectMessage createObjectMessage() throws JMSException {
        return this.session.createObjectMessage();
    }

    /**
     * 
     * Creates a new <code>javax.jms.TextMessage</code> with the following JMS properties.
     * <ul>
     * <li>JMSDeliveryMode= <code>DeliveryMode.NON_PERSISTENT</code></li>
     * <li>JMSPriority=6</li>
     * 
     * <li>JMSReplyTo= <i>temporary destination </i></li>
     * 
     * </ul>
     * 
     * and the following message properties:
     * 
     * <ul>
     * 
     * <li>MESSAGE_TYPE=REQUEST</li>
     * 
     * <li>REPLYTO_DESTINATION_TYPE=QUEUE | TOPIC</li>
     * 
     * <li>APPLICATION_TYPE=OMS_VF</li>
     * 
     * <li>APPLICATION_ID=OMS</li>
     * 
     * <li>VERSION_ID=1</li>
     * 
     * </ul>
     * 
     * 
     * 
     * @return the newly-created <code>TextMessage</code>
     * 
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */
    public TextMessage createTextMessage() throws JMSException {

        return createTextMessage(getResponseDestination());

    }

    /**
     * 
     * Sends a new <code>TextMessage</code> (with the passed String as the message's text, and synchronously waits for a response. The
     * 
     * response <code>Message</code> is expected to be an instance of <code>TextMessage</code> with well-formed XML as it's payload.
     * 
     * 
     * 
     * @param text
     * 
     *            the message text
     * 
     * @return the response TextMessage's XML text represented as a non-namespace-aware, non-validating <code>Document</code>
     * 
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */

    public Document sendMessage(final String text) throws JMSException {

        return sendMessage(text, false);

    }

    /**
     * 
     * Sends a new <code>TextMessage</code> (with the passed String as the message's text, and synchronously waits for a response. The
     * 
     * response <code>Message</code> is expected to be an instance of <code>TextMessage</code> with well-formed XML as it's payload.
     * 
     * 
     * 
     * @param text
     * 
     *            the message text
     * 
     * @param namespaceAware
     * 
     *            <code>true</code> if the response DOM instance should be namespace-aware; <code>false</code> otherwise.
     * 
     * @return the response TextMessage's XML text represented as a non-validating <code>Document</code>
     * 
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */

    public Document sendMessage(final String text, final boolean namespaceAware) throws JMSException {

        return sendMessage(text, namespaceAware, JMSHelper.DEFAULT_TIMEOUT);

    }

    /**
     * 
     * Sends a new <code>TextMessage</code> (with the passed String as the message's text, and synchronously waits for a response. The
     * 
     * response <code>Message</code> is expected to be an instance of <code>TextMessage</code> with well-formed XML as it's payload.
     * 
     * 
     * 
     * @param text
     * 
     *            the message text
     * 
     * @param namespaceAware
     * 
     *            <code>true</code> if the response DOM instance should be namespace-aware; <code>false</code> otherwise.
     * 
     * @param timeout
     * 
     *            the timeout value.
     * 
     * @return the response TextMessage's XML text represented as a non-validating <code>Document</code>
     * 
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */

    public Document sendMessage(final String text, final boolean namespaceAware, final long timeout) throws JMSException {

        checkInitialized();

        final Message message = sendJMSMessage(text);
        if (message != null) {
            if (message instanceof TextMessage) {
                try {
                    return XMLHelper.parseText(((TextMessage) message).getText(), namespaceAware);
                } catch (final Exception exception) {
                    log.error("Error parsing response", exception);
                }

                throw new JMSException("Error parsing response. " + ((TextMessage) message).getText());
            }
            throw new JMSException("Error. Response message was not a TextMessage");
        }

        throw new JMSException("Timeout. Response message not received after [" + timeout + "] milliseconds");
    }

    /**
     * 
     * @param text
     *            The text message to be send.
     * 
     * @return the response Message.
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */
    public Message sendJMSMessage(final String text) throws JMSException {
        return sendJMSMessage(text, DEFAULT_TIMEOUT);

    }

    /**
     * 
     * Sends a new <code>TextMessage</code> with the passed String as the message's text, and synchronously waits for a response.
     * 
     * @param text
     *            the message text.
     * 
     * @param timeout
     *            the timeout value.
     * 
     * @return the response Message.
     * 
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */
    public Message sendJMSMessage(final String text, final long timeout) throws JMSException {

        checkInitialized();

        final TextMessage textMessage = createTextMessage();

        textMessage.setText(text);

        final Message message = sendMessage(textMessage, timeout);

        return message;

    }

    /**
     * 
     * @param message
     * 
     *            The message to be send.
     * 
     * @return the response message.
     * 
     * @throws JMSException
     * 
     *             When there is JMS exception.
     */

    public Message sendMessage(final Message message) throws JMSException {

        return sendMessage(message, DEFAULT_TIMEOUT);

    }

    /**
     * 
     * Sends the passed <code>Message</code> and synchronously waits for a response.
     * 
     * <p>
     * 
     * <i>Note that if <code>message</code> has the <code>JMSReplyTo</code> property set, it will be overridden by this method. </i>
     * 
     * 
     * 
     * @param message
     * 
     *            the <code>Message</code> to send.
     * 
     * @param timeout
     * 
     *            The timeout value.
     * 
     * @return the response <code>Message</code>.
     * 
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */

    public Message sendMessage(final Message message, final long timeout) throws JMSException {

        checkInitialized();

        message.setJMSReplyTo(getResponseDestination());

        this.producer.setPriority(message.getJMSPriority());

        if (this.producer instanceof TopicPublisher) {

            ((TopicPublisher) this.producer).publish(message);

        } else {

            this.producer.send(message);

        }

        final Message response = this.consumer.receive(timeout);

        return response;

    }

    /**
     * 
     * Sends a new <code>TextMessage</code> with the passed String as the message's text, but does not wait for a response.
     * 
     * <p>
     * 
     * This method is suitable for use when an event needs to be generated for which no response is expected/required.
     * 
     * 
     * 
     * @param text
     * 
     *            the message text.
     * 
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */

    public void sendMessageNoWait(final String text) throws JMSException {

        checkInitialized();

        try {

            final TextMessage textMessage = createTextMessage(null);

            textMessage.setText(text);

            sendMessageNoResponse(textMessage);

        } catch (final Exception x) {

            // retry once, just in case there are temporary connection problems

            //

            log.warn("Encountered problem sending message to destination[" + getDestination() + "] - retrying", x);

            initialize();

            final TextMessage textMessage = createTextMessage(null);

            textMessage.setText(text);

            sendMessageNoResponse(textMessage);

        }

    }

    /**
     * 
     * Sends a new <code>TextMessage</code> with the passed String as the message's text, but does not wait for a response.
     * 
     * <p>
     * 
     * This method is suitable for use when an event needs to be generated for which no response is expected/required.
     * 
     * 
     * 
     * @param text
     * 
     *            the message text.
     * 
     * @param eventType
     * 
     *            Sets a JMS property called "ORACLE_CGBU_EVENT_TYPE" to the value provided e.g. OrderRemoved.Event
     * 
     * @param priority
     * 
     *            The priority setting.
     * 
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */

    public void sendMessageNoWait(final String text, final String eventType, final int priority) throws JMSException {

        checkInitialized();

        try {
            final TextMessage textMessage = createTextMessage(null);
            textMessage.setText(text);
            textMessage.setJMSPriority(priority);
            sendMessageNoResponse(textMessage);
        } catch (final Exception x) {
            // retry
            log.warn("Encountered problem sending message to destination[" + getDestination() + "] - retrying", x);
            initialize();
            final TextMessage textMessage = createTextMessage(null);
            textMessage.setStringProperty("MSLV_EVENT_TYPE", eventType);
            textMessage.setText(text);
            textMessage.setJMSPriority(priority);
            sendMessageNoResponse(textMessage);

        }

    }

    /**
     * 
     * Sends a new <code>ObjectMessage</code> with the passed String as the message's text, but does not wait for a response.
     * 
     * <p>
     * 
     * This method is suitable for use when an event needs to be generated for which no response is expected/required.
     * 
     * 
     * 
     * @param object
     * 
     *            the message object.
     * 
     * @param eventType
     * 
     *            Sets a JMS property called "MSLV_EVENT_TYPE" to the value provided
     * 
     * @param priority
     * 
     *            The priority setting.
     * 
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */
    public void sendMessageNoWait(final Serializable object, final String eventType, final int priority) throws JMSException {

        checkInitialized();
        final ObjectMessage objectMessage = this.session.createObjectMessage();
        objectMessage.setStringProperty("MSLV_EVENT_TYPE", eventType);
        objectMessage.setObject(object);
        objectMessage.setJMSPriority(priority);
        sendMessageNoResponse(objectMessage);
    }

    public void sendMessageNoWait(final Serializable object, final String eventType, final int priority, final long deliveryDelay)

    throws JMSException {

        checkInitialized();

        final ObjectMessage objectMessage = this.session.createObjectMessage();
        objectMessage.setStringProperty("MSLV_EVENT_TYPE", eventType);
        objectMessage.setObject(object);
        objectMessage.setJMSPriority(priority);
        sendMessageNoResponse(objectMessage, deliveryDelay);
    }

    /**
     * 
     * Sends a new <code>TextMessage</code> with the passed String as the message's text, but does not wait for a response.
     * 
     * <p>
     * 
     * This method is suitable for use when an event needs to be generated for which no response is expected/required.
     * 
     * 
     * 
     * @param text
     * 
     *            the message text.
     * 
     * @param eventType
     * 
     *            Sets a JMS property called "MSLV_EVENT_TYPE" to the value provided e.g. OrderRemoved.Event
     * 
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */
    public void sendMessageNoWait(final String text, final String eventType) throws JMSException {
        checkInitialized();
        final TextMessage textMessage = createTextMessage(null);
        textMessage.setStringProperty("MSLV_EVENT_TYPE", eventType);
        textMessage.setText(text);
        sendMessageNoResponse(textMessage);
    }

    /**
     * 
     * Sends the passed <code>Message</code> but does not wait for a response, nor does it set the JMSReplyTo field. It delays the delivery
     * 
     * of the message by the specified number of milliseconds.
     * 
     * 
     * 
     * @param message
     * 
     *            the <code>Message</code> to send.
     * 
     * @param delay
     * 
     *            number of milliseconds by which to delay the delivery.
     * 
     * @throws JMSException
     * 
     *             if an underlying JMSException occurs.
     */
    public void sendMessageNoResponse(final Message message, final long deliveryDelay) throws JMSException {

        checkInitialized();
        this.producer.setPriority(message.getJMSPriority());
        ((WLMessageProducer) this.producer).setTimeToDeliver(deliveryDelay);
        if (this.producer instanceof TopicPublisher) {
            ((TopicPublisher) this.producer).publish(message);
        } else {
            this.producer.send(message);
        }
    }

    /**
     * 
     * Sends the passed <code>Message</code> but does not wait for a response, nor does it set the JMSReplyTo field.
     * 
     * @param message
     *            the <code>Message</code> to send.
     * 
     * @throws JMSException
     *             if an underlying JMSException occurs.
     */
    public void sendMessageNoResponse(final Message message) throws JMSException {
        sendMessageNoResponse(message, 0L);
    }

    /**
     * 
     * Releases JMS resources reserved by this instance.
     */
    public void close() {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Calling JMSHelper.close");
            }

            if (this.connection != null) {
                this.connection.close();
            }
        } catch (final Exception exception) {
            log.warn("Failed to close connections", exception);
        } finally {
            this.initialized = false;
        }

    }

    /**
     * 
     * Check if this instance of JMSHelper has been initial, if not raise IllegalStateException.
     */
    private void checkInitialized() {
        if (!this.initialized) {
            throw new IllegalStateException("This method cannot be called after close()");
        }
    }

    private static ThreadLocalMap<MultiKey, JMSHelper> threadLocalJMSHelper = new ThreadLocalMap<MultiKey, JMSHelper>();

    /**
     * 
     * Obtain an instance of the JMSHelper. This is a thread-local instance that MUST NOT be shared across threads. To ensure the result of
     * this call does not get shared across threads, never assign the result of this call to a class member variable.
     * 
     * @param destination
     *            The JNDI name of the destination.
     * 
     * @return the JMSHelper
     */
    public static JMSHelper instance(final String destination) {
        return instance(DEFAULT_QUEUE_FACTORY_JNDI, destination);
    }

    /**
     * 
     * Obtain an instance of the JMSHelper. This is a thread-local instance that MUST NOT be shared across threads. To ensure the result of
     * this call does not get shared across threads, never assign the result of this call to a class member variable.
     * 
     * @param factoryName
     *            The factory name.
     * 
     * @param destination
     *            The JNDI name of the destination.
     * 
     * @return the JMSHelper
     */
    public static JMSHelper instance(final String factoryName, final String destination) {
        JMSHelper result = threadLocalJMSHelper.get(new MultiKey(factoryName, destination));
        if (result == null) {
            result = new JMSHelper(factoryName, destination);
            threadLocalJMSHelper.put(new MultiKey(factoryName, destination), result);
        }

        return result;

    }

    /**
     * 
     * @param destination
     * 
     *            the destination to set
     */
    void setDestination(final String destination) {
        this.destination = destination;
    }

    /**
     * 
     * @return the destination
     */
    String getDestination() {
        return this.destination;
    }

    /**
     * 
     * @param factoryName
     *            the factoryName to set
     */
    void setFactoryName(final String factoryName) {
        this.factoryName = factoryName;
    }

    /**
     * 
     * @return the factoryName
     */
    String getFactoryName() {
        return this.factoryName;
    }

    /**
     * 
     * Creates byte message to be populated by the caller.
     * 
     * @return newly-created <code>ObjectMessage</code>
     * 
     * @throws JMSException
     *             When there is an exception.
     */
    public BytesMessage createBytesMessage() throws JMSException {
        return this.session.createBytesMessage();
    }

    /**
     * Clear the instance of the JMSHelper. This is a thread-local instance that MUST NOT be shared across threads. To ensure the result of
     * this call does not get shared across threads, never assign the result of this call to a class member variable.
     * 
     * @param destination
     *            The destination JNDI name.
     */
    public static void clearInstance(final String destination) {
        clearInstance(DEFAULT_QUEUE_FACTORY_JNDI, destination);
    }

    /**
     * Clear the instance of the JMSHelper. This is a thread-local instance that MUST NOT be shared across threads. To ensure the result of
     * this call does not get shared across threads, never assign the result of this call to a class member variable.
     * 
     * @param factoryName
     *            The factory name.
     * 
     * @param destination
     *            The destination JNDI name.
     */
    public static void clearInstance(final String factoryName, final String destination) {
        final JMSHelper result = threadLocalJMSHelper.get(new MultiKey(factoryName, destination));
        if (result != null) {
            result.close();
            threadLocalJMSHelper.remove(new MultiKey(factoryName, destination));
        }
    }

    /**
     * Obtain an instance of the JMSHelper. This is a thread-local instance that MUST NOT be shared across threads. To ensure the result of
     * this call does not get shared across threads, never assign the result of this call to a class member variable.
     * 
     * @param destination
     *            The JNDI name of the destination.
     * 
     * @param creationTime
     *            The time of creation to be validate and set.
     * 
     * @return the JMSHelper
     */
    public static JMSHelper instance(final String destination, final long creationTime) {
        return instance(DEFAULT_QUEUE_FACTORY_JNDI, destination, creationTime);
    }

    /**
     * Obtain an instance of the JMSHelper. This is a thread-local instance that MUST NOT be shared across threads. To ensure the result of
     * this call does not get shared across threads, never assign the result of this call to a class member variable.
     * 
     * @param factoryName
     *            The factory name.
     * 
     * @param destination
     *            The JNDI name of the destination.
     * 
     * @param creationTime
     *            The time of creation to be validate and set.
     * 
     * @return the JMSHelper
     */
    public static JMSHelper instance(final String factoryName, final String destination, final long creationTime) {
        JMSHelper result = threadLocalJMSHelper.get(new MultiKey(factoryName, destination));

        if (result != null && result.getCreationTime() != creationTime) {
            result.close();
            result = null;
        }

        if (result == null) {
            result = new JMSHelper(factoryName, destination);
            result.setCreationTime(creationTime);

            threadLocalJMSHelper.put(new MultiKey(factoryName, destination), result);
        }

        return result;

    }

    /**
     * 
     * @return the creation time of this cluster member.
     */
    public long getCreationTime() {
        return this.creationTime;
    }

    /**
     * 
     * @param creationTime
     *            The creation time.
     */
    public void setCreationTime(final long creationTime) {
        this.creationTime = creationTime;
    }

}
