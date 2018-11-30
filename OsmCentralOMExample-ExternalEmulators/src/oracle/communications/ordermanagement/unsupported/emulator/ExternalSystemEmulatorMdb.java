/*
 * Copyright (c) 2008, 2009, Oracle and/or its affiliates. All rights reserved.
 */
package oracle.communications.ordermanagement.unsupported.emulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicLong;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.naming.InitialContext;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import oracle.communications.ordermanagement.util.jms.JMSHelper;
import oracle.communications.ordermanagement.util.transform.XQueryTemplates;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileSystemException;
import org.w3c.dom.Document;

import com.mslv.oms.util.xml.XMLHelper;

/**
 * This is an implementation of an external system emulator.
 * 
 * Note that this implementation presently depends on some OSM code which is not part of the formal OSM APIs and therefore must not be
 * shared with customers or 3rd parties.
 * 
 */
public class ExternalSystemEmulatorMdb implements MessageDrivenBean, MessageListener {

    /**
     * TODO: Make this configurable
     */
    private static final int LOG_DIR_CREATE_INTERVAL = 60000;

    private static final Log LOG = LogFactory.getLog(ExternalSystemEmulatorMdb.class);

    /**
     * 
     */
    private static final long serialVersionUID = 7282333526863306703L;

    private MessageDrivenContext ctx = null;

    private String destinationName = null;

    private URI xqueryUri = null;
    private Templates templates;

    private long lastResourceCheckTime = 0;
    private long lastLoadTime = 0;
    private final static long RESOURCE_CHECK_GRANULARITY = 500;

    private static long lastLogDirCreated = 0;
    private static File logDir = null;

    private final static AtomicLong messageCounter = new AtomicLong();

    protected static File getLogDir() throws IOException {
        synchronized (ExternalSystemEmulatorMdb.class) {

            if (logDir == null || lastLogDirCreated + LOG_DIR_CREATE_INTERVAL <= System.currentTimeMillis()) {

                final File tmpFile = File.createTempFile("osm_ext_emulator", null);
                tmpFile.delete();
                lastLogDirCreated = System.currentTimeMillis();
                logDir = new File(tmpFile.toURI());
                logDir.mkdirs();
                LOG.info("Emulator message log dir is now[" + logDir.toURI() + "]");
            }
            return logDir;
        }
    }

    public void ejbCreate() throws CreateException {
        LOG.info(this.getClass().getName() + ".ejbCreate()");

        try {

            final InitialContext initialCtx = new InitialContext();

            this.destinationName = (String) initialCtx.lookup("java:comp/env/DESTINATION_JNDI");

            xqueryUri = new URI((String) initialCtx.lookup("java:comp/env/XQUERY_SCRIPT"));

        } catch (final Exception x) {

            throw new RuntimeException(x.getMessage(), x);

        }

    }

    public void ejbActivate() {
    }

    public void ejbRemove() throws EJBException {
        LOG.info(this.getClass().getName() + ".ejbRemove()");
    }

    public void ejbPassivate() {
    }

    public void setMessageDrivenContext(final MessageDrivenContext ctx) throws EJBException {
        this.ctx = ctx;

    }

    public void onMessage(final Message message) {
    	synchronized (ExternalSystemEmulatorMdb.class) {
        try {

            LOG.info("Received a message" + SystemUtils.LINE_SEPARATOR + message);

            if (message instanceof TextMessage) {
                final TextMessage textMessage = (TextMessage) message;
                logMessage(textMessage, textMessage.getJMSDestination().toString(), "request");
                final String body = textMessage.getText();
                final Document doc = XMLHelper.parseText(body);

                LOG.info(XMLHelper.prettyPrintXML(doc));

                final JMSHelper helper = JMSHelper.instance(this.destinationName);
                final TextMessage response = helper.createTextMessage();
                response.setStringProperty("MESSAGE_TYPE", "EMULATOR_RESPONSE");
                response.setStringProperty("APPLICATION_TYPE", this.getClass().getName());
                response.setStringProperty("APPLICATION_ID", this.getClass().getName());
                response.setIntProperty("VERSION_ID", 1);
                response.setJMSDeliveryMode(message.getJMSDeliveryMode());

                LOG.info("JMSCorrelationID=" + message.getJMSCorrelationID());
                response.setJMSCorrelationID(message.getJMSCorrelationID());
                response.setJMSPriority(message.getJMSPriority());

                transformRequestToResponse(body, response);
                logMessage(response, textMessage.getJMSDestination().toString(), "response");
                LOG.info(response.getText());
                helper.sendMessageNoResponse(response, 250);

            }
        } catch (final Exception e) {
            LOG.error("onMessage:Exception:[" + e.getMessage() + "]", e);
            this.ctx.setRollbackOnly();
        }
    	}
    }

    private void logMessage(final TextMessage message, final String destination, final String type) throws IOException,
            FileSystemException, JMSException {
        String xmlMessage;
        try {
            final Document doc = XMLHelper.parseText(message.getText());
            xmlMessage = XMLHelper.prettyPrintXML(doc);
        } catch (final Exception x) {
            xmlMessage = x.getMessage();
        }

        final File logParentDir = new File(FilenameUtils.concat(getLogDir().getPath(), StringUtils.substringAfterLast(destination, "!")));
        final boolean result = logParentDir.mkdirs();
        if (LOG.isDebugEnabled()) {
            LOG.debug("dir created " + result + " " + logParentDir.toURI() + " " + getLogDir().getPath());
        }
        final File logFile = new File(FilenameUtils.concat(logParentDir.getPath(), messageCounter.addAndGet(1) + "_" + type + ".xml"));
        logFile.createNewFile();
        final OutputStream ostream = new FileOutputStream(logFile);
        IOUtils.write(xmlMessage, ostream);
        IOUtils.closeQuietly(ostream);
    }

    protected Templates getTemplates(final URL resource) throws IOException, TransformerConfigurationException {
        final long currentCheckTime = System.currentTimeMillis();
        if (templates == null) {
            lastLoadTime = currentCheckTime;
            createTemplates(resource);
        }

        if (lastResourceCheckTime + RESOURCE_CHECK_GRANULARITY < currentCheckTime) {
            if (resource.openConnection().getDate() > lastLoadTime) {
                lastLoadTime = currentCheckTime;
                createTemplates(resource);
            }
        }

        lastResourceCheckTime = currentCheckTime;
        return templates;
    }

    private void createTemplates(final URL resource) throws IOException, TransformerConfigurationException {
        InputStream resourceStream = null;
        Reader reader = null;
        try {
            resourceStream = resource.openStream();
            reader = new BufferedReader(new InputStreamReader(resourceStream));
            templates = new XQueryTemplates(reader, LOG);
        } finally {
            IOUtils.closeQuietly(reader);
            IOUtils.closeQuietly(resourceStream);
        }
    }

    protected void transformRequestToResponse(final String inputData, final TextMessage response) throws IOException, JMSException,
            TransformerException {

        try {
        	LOG.info("+++++++++++++++++++++++++ XQUERY: " + xqueryUri.toString());
        	
        	ClassLoader cl = Thread.currentThread().getContextClassLoader();
        	//ClassLoader cl = this.getClass().getClassLoader();
        	if (cl != null){
        		LOG.info("+++++++++++++++++++++++++ CLASSLOADER IS NOT NULL");
        	
        	}
        	boolean found = false;
        	URL resource = null;
        	Enumeration<URL> res = cl.getResources("META-INF/" + this.xqueryUri.toString());
        	LOG.info("--------------------- LOOKING FOR URLS -------------------------");
        	while (res.hasMoreElements()){
        		resource = res.nextElement();
        		found = true;
        		LOG.info("--------------------- URL: " + resource.toExternalForm() );
        	}
        	if (!found){
             resource =
                    cl.getResource(
                        FilenameUtils.concat("/META-INF/", this.xqueryUri.toString()));
            if (resource == null) {
                throw new IOException("Cannot locate resource[" + resource + "]");
            }
        	}
            templates = getTemplates(resource);

            final Transformer transformer = templates.newTransformer();

            transformer.setParameter(XQueryTemplates.XQUERY_JAVA_BINDING_OUTBOUND_MESSAGE_NAMESPACE_PREFIX, response);
            transformer.setParameter(XQueryTemplates.XQUERY_JAVA_BINDING_LOG_NAMESPACE_PREFIX, LOG);

            // create Source...
            final StreamSource source = new StreamSource(new StringReader(inputData));

            // create Result...
            final StringWriter stringWriter = new StringWriter();
            final StreamResult result = new StreamResult(stringWriter);

            transformer.transform(source, result);

            // return result...
            final String transformed = stringWriter.getBuffer().toString();
            if (transformed != null && transformed.length() > 0) {
                response.setText(transformed);
            }
        } catch (final TransformerException e) {
            final String err =
                    "Unable to create Transformer object for Script resource '" + xqueryUri + "'; reported Script exception location: "
                            + e.getLocationAsString();
            LOG.error(err, e);
            throw e;
        }

    }
}
