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
let $serviceLine := $order/oms:_root/oms:ControlData/oms:Functions/oms:ProvisioningFunction/oms:orderItem[oms:orderItemRef/oms:productClass/text() = 'Fixed Service Plan Class']
let $specs :=  $serviceLine/oms:orderItemRef/oms:lineItemPayload/im:salesOrderLine/im:itemReference/im:specificationGroup/im:specification
let $serviceId :=  $serviceLine/oms:orderItemRef/oms:serviceId/text()

let $features := $order/oms:_root/oms:ControlData/oms:Functions/oms:ProvisioningFunction/oms:orderItem[oms:orderItemRef/oms:productClass/text() = 'Fixed Service Feature Class']
let $requestedDate := $serviceLine/oms:orderItemRef/oms:requestedDeliveryDate/text()
return
(

log:info($log,fn:concat('fixed provisioning cancel request[',/oms:GetOrder.Response/oms:OrderID,'] count[',count(/oms:GetOrder.Response/oms:_root/oms:data),']')),
<cancelOrderFixed xmlns="http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/fixed"
 xmlns:ns2="http://xmlns.oracle.com/InputMessage"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
     <order>
        <numSalesOrder>{$header/oms:numSalesOrder/text()}</numSalesOrder>
        <numOrder>{fn:concat($order/oms:OrderID,'-',$order/oms:OrderHistID)}</numOrder>
        <typeOrder>{$header/oms:typeOrder/text()}</typeOrder>
        <numFixed>{$serviceId}</numFixed>
        <category>{$account/oms:category/text()}</category>
        <typeService>
        {
            for $feature in $features
            return
                <service>{$feature/oms:orderItemRef/oms:lineItemName/text()}</service>
        }
        </typeService>
    </order>
</cancelOrderFixed>
)