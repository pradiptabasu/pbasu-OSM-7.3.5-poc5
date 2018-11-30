(: only require to be declared when editing with Oxygen :)
declare namespace automator = "java:oracle.communications.ordermanagement.automation.plugin.ScriptSenderContextInvocation";
(: only require to be declared when editing with Oxygen :)
declare namespace context = "java:com.mslv.oms.automation.TaskContext";
(: only require to be declared when editing with Oxygen :)
declare namespace log = "java:org.apache.commons.logging.Log";
declare namespace oms="urn:com:metasolv:oms:xmlapi:1";
declare namespace im="http://xmlns.oracle.com/InputMessage";
declare namespace saxon="http://saxon.sf.net/";
declare namespace xsl="http://www.w3.org/1999/XSL/Transform";
 
declare variable $automator external; 
declare variable $context external;
declare variable $log external;  

let $order := /oms:GetOrder.Response
let $customer := $order/oms:_root/oms:CustomerDetails
let $account := $order/oms:_root/oms:AccountDetails
let $header := $order/oms:_root/oms:OrderHeader
let $serviceLine := $order/oms:_root/oms:ControlData/oms:Functions/oms:ProvisioningFunction/oms:orderItem[oms:orderItemRef/oms:productClass/text() = 'High Speed Internet Service Class']
let $specs :=  $serviceLine/oms:orderItemRef/oms:lineItemPayload/im:salesOrderLine/im:itemReference/im:specificationGroup/im:specification
let $serviceId :=  $serviceLine/oms:orderItemRef/oms:serviceId/text()
let $requestedDate := $serviceLine/oms:orderItemRef/oms:requestedDeliveryDate/text()
let $inputDocument := saxon:serialize(fn:root(), <xsl:output method='xml' omit-xml-declaration='yes' indent='yes' saxon:indent-spaces='4'/>)
return
(

log:info($log,fn:concat('hello from order[',/oms:GetOrder.Response/oms:OrderID,'] count[',count(/oms:GetOrder.Response/oms:_root/oms:data),']')),

<cancelOrderAdsl xmlns="http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/dsl_region1"
 xmlns:ns2="http://xmlns.oracle.com/InputMessage"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">

    
     <order>
    <numSalesOrder>{$header/oms:numSalesOrder/text()}</numSalesOrder>
    <numOrder>{fn:concat($order/oms:OrderID,'-',$order/oms:OrderHistID)}</numOrder>
    <typeOrder>{$header/oms:typeOrder/text()}</typeOrder>
    <typeADSL>{$specs[im:name/text() = 'typeADSL']/im:value/text()}</typeADSL>
    <numCircuit>{$serviceId}</numCircuit>
    <category>{$account/oms:category/text()}</category>
    <idProductADSL>TODO</idProductADSL>
   
</order>
</cancelOrderAdsl>
)