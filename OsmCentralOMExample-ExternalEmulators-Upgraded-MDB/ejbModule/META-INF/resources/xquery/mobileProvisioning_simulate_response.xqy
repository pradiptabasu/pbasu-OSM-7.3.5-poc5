(: only require to be declared when editing with Oxygen :)
declare namespace log = "java:org.apache.commons.logging.Log";
declare namespace logFactory = "java:org.apache.commons.logging.LogFactory";
declare namespace java="http://xml.apache.org/xslt/java";

declare namespace req = "http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/mobile";
(: declare references to external objects we will use to process this transformation :) 
declare variable $log external;  

(: declare some variables pointing to relevant parts of the order for our transformation :)
let $order := fn:root()/req:mobileOrderActivation/req:order
let $cancel := fn:root()/req:cancelOrderMobile/req:order

return
(
if (exists($order)) then (
<orderResponse xmlns="http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/interactionResponse"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <numSalesOrder>{$order/req:numSalesOrder/text()}</numSalesOrder>
    <numOrder>{$order/req:numOrder/text()}</numOrder>
    <typeOrder>{$order/req:typeOrder/text()}</typeOrder>
    <errorCode>0</errorCode>
    <message>OK</message>
    <status>Provisioning_Completed</status>
</orderResponse>
) else (
<cancelResponse xmlns="http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/interactionResponse"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <numSalesOrder>{$cancel/req:numSalesOrder/text()}</numSalesOrder>
    <numOrder>{$cancel/req:numOrder/text()}</numOrder>
    <typeOrder>{$cancel/req:typeOrder/text()}</typeOrder>
    <errorCode>0</errorCode>
    <message>CANCEL</message>
    <status>C</status>
</cancelResponse>
)
)