package oracle.communications.ordermanagement.util.transform;

import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

import javax.jms.TextMessage;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;

import net.sf.saxon.query.ModuleURIResolver;
import net.sf.saxon.query.StaticQueryContext;
import net.sf.saxon.query.XQueryExpression;
import net.sf.saxon.trans.XPathException;
import oracle.communications.ordermanagement.rule.CatalogModuleUriResolver;

import org.apache.commons.logging.Log;

public class XQueryTemplates implements Templates {

    public static final String XQUERY_JAVA_BINDING_OUTBOUND_MESSAGE_NAMESPACE_PREFIX = "outboundMessage";
    private static final String XQUERY_JAVA_BINDING_OUTBOUND_MESSAGE_NAMESPACE = "java:" + TextMessage.class.getName();
    public static final String XQUERY_JAVA_BINDING_EMULATOR_NAMESPACE_PREFIX = "emulator";
    public static final String XQUERY_JAVA_BINDING_LOG_NAMESPACE_PREFIX = "log";

    private XQueryExpression xqueryExpression;

    /**
     * Constructor. Create a XQueryExpression by the given reader that point to the XQuery script resource and declare all passive name
     * space provided by the given XQueryPluginBindingNamespaceProvider to the StaticQueryContext.
     * 
     * @param reader
     *            The reader point to the script resource.
     * @param namespaceProvider
     *            The object implements the XQueryPluginBindingNamespaceProvider interface which provide namespace for register to the
     *            StaticQueryContext context.
     * @throws TransformerConfigurationException
     *             When there is and XQuery script compilation error.
     */
    public XQueryTemplates(final Reader reader, final Log log) throws TransformerConfigurationException {
        xqueryExpression = null;
        final StaticQueryContext staticContext = new StaticQueryContext(XQueryTransformer.CONFIGURATION);
        try {
            staticContext.declareNamespace(XQUERY_JAVA_BINDING_OUTBOUND_MESSAGE_NAMESPACE_PREFIX,
                XQUERY_JAVA_BINDING_OUTBOUND_MESSAGE_NAMESPACE);
            staticContext.declareNamespace(XQUERY_JAVA_BINDING_LOG_NAMESPACE_PREFIX, "java:" + log.getClass().getName());
            staticContext.setModuleURIResolver((ModuleURIResolver)CatalogModuleUriResolver.getSystemCatalogUriResolver());
            xqueryExpression = staticContext.compileQuery(reader);
        } catch (final IOException e) {
            throw new TransformerConfigurationException(e);
        } catch (final XPathException e) {
            throw new TransformerConfigurationException(e);
        } catch (final Exception e) {
            throw new TransformerConfigurationException(e);
        }
    }

    /**
     * The getOutputProperties is unsupported. Calling this will throw {@link UnsupportedOperationException}.
     * 
     * @return N.A
     * 
     * @see javax.xml.transform.Templates#getOutputProperties()
     */
    public Properties getOutputProperties() {
        throw new UnsupportedOperationException();
    }

    /**
     * @return the XQueryTransformer implementing the Transformer interface.
     * 
     * @throws TransformerConfigurationException
     *             When error.
     * 
     * @see javax.xml.transform.Templates#newTransformer()
     */
    public Transformer newTransformer() throws TransformerConfigurationException {
        return new XQueryTransformer(this.xqueryExpression);
    }
}
