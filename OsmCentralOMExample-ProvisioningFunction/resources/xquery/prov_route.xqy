(: only require to be declared when editing with Oxygen :)
declare namespace automator = "java:oracle.communications.ordermanagement.automation.plugin.ScriptReceiverContextInvocation";
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
let $function := $order/oms:_root/oms:ControlData/oms:Functions/oms:ProvisioningFunction


return (
    automator:setUpdateOrder($automator,"false"),
    log:info($log,$function/oms:componentKey/text()),
    if (fn:starts-with($function/oms:componentKey/text(), "ProvisioningFunction.MobileProvisioningSystem")) then
    (
    context:completeTaskOnExit($context,"route_to_mobile"),
    <foo/>
    )
    else if (fn:starts-with($function/oms:componentKey/text(), "ProvisioningFunction.FixedProvisioningSystem")) then
    (
    context:completeTaskOnExit($context,"route_to_fixed"),
    <foo/>
    )
    else if (fn:starts-with($function/oms:componentKey/text(), "ProvisioningFunction.DSLProvisioningSystem_Region2")) then
    (
    context:completeTaskOnExit($context,"route_to_osm"),
    <foo/>
    )
    else if (fn:starts-with($function/oms:componentKey/text(), "ProvisioningFunction.DSLProvisioningSystem_Region1")) then
    (
    context:completeTaskOnExit($context,"route_to_expediter"),
    <foo/>
    )
    else()
)

