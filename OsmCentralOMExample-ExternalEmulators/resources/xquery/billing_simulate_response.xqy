(: only require to be declared when editing with Oxygen :)
declare namespace log = "java:org.apache.commons.logging.Log";

declare namespace req = "http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/billing";
(: declare references to external objects we will use to process this transformation :) 
declare variable $log external;  

(: declare some variables pointing to relevant parts of the order for our transformation :)
let $order := fn:root()/req:configureBilling/req:order
let $cancel :=fn:root()/req:cancelBilling/req:order

return
(
log:info($log,'creating billing response'),
if (exists($cancel)) then (
<cancelResponse xmlns="http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/interactionResponse"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <numSalesOrder>{$cancel/req:numSalesOrder/text()}</numSalesOrder>
    <numOrder>{$cancel/req:numOrder/text()}</numOrder>
    <typeOrder>{$cancel/req:typeOrder/text()}</typeOrder>
    <errorCode>0</errorCode>
    <message>CANCEL</message>
    <status>C</status>
</cancelResponse>) else (
<orderResponse xmlns="http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/interactionResponse"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <numSalesOrder>{$order/req:numSalesOrder/text()}</numSalesOrder>
    <numOrder>{$order/req:numOrder/text()}</numOrder>
    <typeOrder>{$order/req:typeOrder/text()}</typeOrder>
    <errorCode>0</errorCode>
    <message>OK</message>
    <status>Billing_Completed</status>
</orderResponse>
)
)