declare namespace oms="urn:com:metasolv:oms:xmlapi:1";
(: JAVA APIs namespaces :)
declare namespace UUID = "java:java.util.UUID";
(: only require to be declared when editing with Oxygen :)
declare namespace automator = "java:oracle.communications.ordermanagement.automation.plugin.ScriptReceiverContextInvocation";
(: only require to be declared when editing with Oxygen :)
declare namespace context = "java:com.mslv.oms.automation.TaskContext";
declare namespace log = "java:org.apache.commons.logging.Log";

declare namespace res = "http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/interactionResponse";
declare option saxon:output "method=xml";
declare option saxon:output "saxon:indent-spaces=4";

declare variable $automator external;
declare variable $context external;
declare variable $log external;

let $response := fn:root()/res:orderResponse
return
(
<node>
{
automator:setUpdateOrder($automator,"false"),
context:completeTaskOnExit($context,"success")
}
</node>
)