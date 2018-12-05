(: only require to be declared when editing with Oxygen :)
declare namespace log = "java:org.apache.commons.logging.Log";
declare namespace logFactory = "java:org.apache.commons.logging.LogFactory";
declare namespace java="http://xml.apache.org/xslt/java";

declare namespace req = "http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/marketing";
(: declare references to external objects we will use to process this transformation :) 
declare variable $log external;  

(: declare some variables pointing to relevant parts of the order for our transformation :)
let $order := fn:root()/req:order


return
(
<orderResponse xmlns="http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/interactionResponse"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <numSalesOrder>{$order/req:numSalesOrder/text()}</numSalesOrder>
    <numOrder>{$order/req:numOrder/text()}</numOrder>
    <typeOrder>{$order/req:typeOrder/text()}</typeOrder>
    <errorCode>0</errorCode>
<!--    <central>ABC12</central>
    <port>34AB</port> -->
    <message>OK</message>
    <status>Marketing_Completed</status>
</orderResponse>

)