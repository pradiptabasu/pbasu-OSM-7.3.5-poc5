(: only require to be declared when editing with Oxygen :)
declare namespace automator = "java:oracle.communications.ordermanagement.automation.plugin.ScriptSenderContextInvocation";
(: only require to be declared when editing with Oxygen :)
declare namespace context = "java:com.mslv.oms.automation.TaskContext";
(: only require to be declared when editing with Oxygen :)
declare namespace log = "java:org.apache.commons.logging.Log";
declare namespace oms="urn:com:metasolv:oms:xmlapi:1";
declare namespace im="http://xmlns.oracle.com/InputMessage";

 
declare variable $automator external; 
declare variable $context external;
declare variable $log external;  

let $order := /oms:GetOrder.Response
let $customer := $order/oms:_root/oms:CustomerDetails
let $account := $order/oms:_root/oms:AccountDetails
let $header := $order/oms:_root/oms:OrderHeader
let $billingProfile := $order/oms:_root/oms:BillingProfile

return
(

log:info($log,fn:concat('arbor request[',/oms:GetOrder.Response/oms:OrderID,'] count[',count(/oms:GetOrder.Response/oms:_root/oms:data),']')),
<ns1:cancelBilling xmlns:ns1="http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/billing"
 xmlns:ns2="http://xmlns.oracle.com/InputMessage"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <ns1:order>
        <ns1:numSalesOrder>{$header/oms:numSalesOrder/text()}</ns1:numSalesOrder>
        <ns1:numOrder>{fn:concat($order/oms:OrderID,'-',$order/oms:OrderHistID)}</ns1:numOrder>
        <ns1:typeOrder>{$header/oms:typeOrder/text()}</ns1:typeOrder>
 
    </ns1:order>
</ns1:cancelBilling>
)