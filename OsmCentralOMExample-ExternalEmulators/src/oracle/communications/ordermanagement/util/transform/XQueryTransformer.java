package oracle.communications.ordermanagement.util.transform;

import java.util.Properties;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.URIResolver;
import javax.xml.transform.stream.StreamSource;

import net.sf.saxon.Configuration;
import net.sf.saxon.dom.DocumentWrapper;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.query.DynamicQueryContext;
import net.sf.saxon.query.XQueryExpression;
import oracle.communications.ordermanagement.rule.CatalogModuleUriResolver;

import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.mslv.oms.util.xml.XMLHelper;

public class XQueryTransformer extends Transformer {

    public static final Configuration CONFIGURATION = new Configuration();

    private final DynamicQueryContext dynamicContext;
    private final XQueryExpression xQueryExpression;

    /**
     * Constructor.
     * 
     * @param xQueryExpression
     *            The XQueryExpression to be associated to this object instance.
     */
    protected XQueryTransformer(final XQueryExpression xQueryExpression) {
        this.xQueryExpression = xQueryExpression;
        this.dynamicContext = new DynamicQueryContext(CONFIGURATION);
    }

    /**
     * Transform the given source by using the embedding XQueryExpression and populating the result.
     * 
     * @param source
     *            (input) The input XML source to be transformed.
     * @param result
     *            (output) The transformation result.
     * @throws TransformerException
     *             When there is transformation error.
     * @see javax.xml.transform.Transformer#transform(javax.xml.transform.Source, javax.xml.transform.Result)
     */
    @Override
    public void transform(final Source source, final Result result) throws TransformerException {
        final StreamSource streamSource = (StreamSource) source;
        try {
            final Node contextNode = XMLHelper.parse(new InputSource(streamSource.getReader()), true, false, true).getDocumentElement();
            dynamicContext.setContextItem(wrapNode(contextNode));
            dynamicContext.setURIResolver(CatalogModuleUriResolver.getSystemCatalogUriResolver());
            xQueryExpression.run(dynamicContext, result, null);
        } catch (final IllegalArgumentException e) {
            throw new TransformerException(e);
        } catch (final SAXException e) {
            throw new TransformerException(e);
        } catch (final Exception e) {
            throw new TransformerException(e);
        }
    }

    /**
     * Helper function that wrap the DOM node to NodeInfo used by DynamicQueryContext.
     * 
     * @param contextNode
     *            The DOM node of the XML document.
     * @return the NodeInfo object to be used by the DynamicQueryContext.
     */
    private NodeInfo wrapNode(final Node contextNode) {
        final DocumentWrapper saxonDoc = new DocumentWrapper(contextNode.getOwnerDocument(), "", CONFIGURATION);
        return saxonDoc.wrap(contextNode);
    }

    /**
     * Associate the parameter value to the given parameter name to the embedding DynamicQueryContext.
     * 
     * @param name
     *            The parameter name.
     * @param value
     *            The parameter value.
     * @see javax.xml.transform.Transformer#setParameter(java.lang.String, java.lang.Object)
     */
    @Override
    public void setParameter(final String name, final Object value) {
        dynamicContext.setParameter(name, value);
    }

    /**
     * Clear all parameters from the embedding DynamicQueryContext.
     * 
     * @see javax.xml.transform.Transformer#clearParameters()
     */
    @Override
    public void clearParameters() {
        dynamicContext.clearParameters();
    }

    /**
     * @return The ErrorListener from the embedding DynamicQueryContext.
     * @see javax.xml.transform.Transformer#getErrorListener()
     */
    @Override
    public ErrorListener getErrorListener() {
        return dynamicContext.getErrorListener();
    }

    /**
     * Get the parameter value associated to the given parameter name from the embedding DynamicQueryContext.
     * 
     * @param name
     *            The name of the parameter.
     * @return the parameter value associated to the given parameter name.
     * 
     * @see javax.xml.transform.Transformer#getParameter(java.lang.String)
     */
    @Override
    public Object getParameter(final String name) {
        return dynamicContext.getParameter(name);
    }

    /**
     * @return the current associated URIResolver from the embedding DynamicQueryContext.
     * @see javax.xml.transform.Transformer#getURIResolver()
     */
    @Override
    public URIResolver getURIResolver() {
        return dynamicContext.getURIResolver();
    }

    /**
     * Register the given ErrorListener to the embedding DynamicQueryContext.
     * 
     * @param listener
     *            The error listener.
     * 
     * @see javax.xml.transform.Transformer#setErrorListener(javax.xml.transform .ErrorListener)
     */
    @Override
    public void setErrorListener(final ErrorListener listener) {
        dynamicContext.setErrorListener(listener);
    }

    /**
     * Associate the given URIResolver to the embedding DynamicQueryContext.
     * 
     * @param resolver
     *            The URIResolver object
     * 
     * @see javax.xml.transform.Transformer#setURIResolver(javax.xml.transform .URIResolver)
     */
    @Override
    public void setURIResolver(final URIResolver resolver) {
        dynamicContext.setURIResolver(resolver);
    }

    /**
     * The getOutputProperties is unsupported. Calling this will throw {@link UnsupportedOperationException}.
     * 
     * @return N.A
     * @see javax.xml.transform.Transformer#getOutputProperties()
     */
    @Override
    public Properties getOutputProperties() {
        throw new UnsupportedOperationException();
    }

    /**
     * The getOutputProperty is unsupported. Calling this will throw {@link UnsupportedOperationException}.
     * 
     * @param name
     *            The name of the property
     * @return N.A.
     * @see javax.xml.transform.Transformer#getOutputProperty(java.lang.String)
     */
    @Override
    public String getOutputProperty(final String name) {
        throw new UnsupportedOperationException();
    }

    /**
     * The setOutputProperties is unsupported. Calling this will throw {@link UnsupportedOperationException}.
     * 
     * @param properties
     *            The properties
     * @see javax.xml.transform.Transformer#setOutputProperties(java.util.Properties)
     */
    @Override
    public void setOutputProperties(final Properties properties) {
        throw new UnsupportedOperationException();

    }

    /**
     * The setOutputProperty is unsupported. Calling this will throw {@link UnsupportedOperationException}.
     * 
     * @param name
     *            The name of the property
     * @param value
     *            the value of the property
     * @see javax.xml.transform.Transformer#setOutputProperty(java.lang.String, java.lang.String)
     */
    @Override
    public void setOutputProperty(final String name, final String value) {
        throw new UnsupportedOperationException();
    }
}
