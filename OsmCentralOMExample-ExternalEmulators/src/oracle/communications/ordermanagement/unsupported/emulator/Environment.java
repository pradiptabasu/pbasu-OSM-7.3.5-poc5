package oracle.communications.ordermanagement.unsupported.emulator;

import java.text.MessageFormat;
import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Logs application startup and shutdown messages.
 * 
 */
public final class Environment {

    private static final Log log = LogFactory.getLog(Environment.class);

    public static final String LINE_SEPARATOR = SystemUtils.LINE_SEPARATOR;

    private static final String DELIM = "*";

    private static final String DELIM_PADDED = DELIM + " ";

    private static String buildVersion = null;

    private static String pluginName;

    private static String postStopPluginMessage;

    private static void buildMessageBuffer(final StringBuffer buf, final String message) {
        buf.append(message);
        buf.append(LINE_SEPARATOR);
    }

    private static void buildMessageBuffer(final StringBuffer buf, final String[] message) {
        try {
            final String asterixLine = StringUtils.repeat(DELIM, maxLineLength(message) + 1);
            buf.append(LINE_SEPARATOR + LINE_SEPARATOR);
            buildMessageBuffer(buf, asterixLine);
            for (final String line : message) {
                buildMessageBuffer(buf, line);
            }
            buf.append(asterixLine);
            buf.append(LINE_SEPARATOR + LINE_SEPARATOR);
        } catch (final Exception x) {
            log.error(x.getLocalizedMessage(), x);
        }

    }

    public static String getProductName() {
        return pluginName;
    }

    private static void logMessage(final String message) {
        try {
            // make sure this is logged
            if (log.isInfoEnabled()) {
                log.info(message);
            } else {
                System.out.println(message);
            }
        } catch (final Exception x) {
            log.error(x.getLocalizedMessage(), x);
        }
    }

    public static int maxLineLength(final String[] stringArray) {
        int maxLineLength = 0;
        for (final String item : stringArray) {
            maxLineLength = Math.max(item.length(), maxLineLength);
        }
        return maxLineLength;
    }

    public static void setPluginName(final String productName) {
        Environment.pluginName = productName;
    }

    private static String getPluginLifecycleMessage(final String msgName) {
        try {
            final String[] preStartMessage =
                    {DELIM_PADDED, DELIM_PADDED + MessageFormat.format(msgName, getProductName(), getBuildVersion(), Locale.getDefault()),
                            DELIM_PADDED};
            final StringBuffer buf = new StringBuffer();
            buildMessageBuffer(buf, preStartMessage);
            return buf.toString();
        } catch (final Exception x) {
            throw new RuntimeException(x);
        }

    }

    private static void logPluginLifecycleMessage(final String msgName) {
        try {
            final String message = getPluginLifecycleMessage(msgName);
            logMessage(message);
        } catch (final Exception x) {
            log.error(x.getLocalizedMessage(), x);
        }
    }

    public static final void logPluginPostStartMessage() {
        logPluginLifecycleMessage("{0} (version {1}) Is Online - All Application Services Are Available");
    }

    public static final void logPluginPostStopMessage() {
        logMessage(postStopPluginMessage);
    }

    public static final void logPluginPreStartMessage() {
        try {
            logPluginLifecycleMessage("{0} (version {1}) - Application Services Are Starting");
        } catch (final Exception x) {
            log.error(x.getLocalizedMessage(), x);
        }

    }

    public static final void logPluginPreStopMessage() {
        logPluginLifecycleMessage("{0} (version {1}) -  Application Services are Stopping");
        // get the post stop message - can't reliably do this when we are actually at
        // post stop because the resources bundle will be unavailable
        //
        postStopPluginMessage = "{0} (version {1}) Is Offline - All Application Services Are Stopped";
    }

    /** Private to prevent instantiation */

    private Environment() {

    }

    /**
     * @param buildVersion
     *            the buildVersion to set
     */
    public static void setBuildVersion(final String buildVersion) {
        Environment.buildVersion = buildVersion;
    }

    /**
     * @return the buildVersion
     */
    private static String getBuildVersion() {
        return buildVersion;
    }

}
