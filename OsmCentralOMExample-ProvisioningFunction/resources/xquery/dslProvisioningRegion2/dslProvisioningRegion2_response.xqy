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

let $cancel := fn:root()/res:cancelResponse
let $responseRoot := if (exists($response)) then ($response) else ($cancel)

let $taskData := fn:root(automator:getOrderAsDOM($automator))/oms:GetOrder.Response
let $component := if (fn:exists($taskData/oms:_root/oms:ControlData/oms:Functions/*/oms:componentKey)) then $taskData/oms:_root/oms:ControlData/oms:Functions/*[fn:position()=1] else ()
let $componentKey := $component/oms:componentKey/text()

let $code := fn:root()/res:orderResponse/res:errorCode/text()
let $status := fn:root()/res:orderResponse/res:status/text()
let $message := fn:root()/res:orderResponse/res:message/text()
let $error := $code = '1'
return (
    log:info($log,'received Provisioning DSL Region 2 response'),
 
    automator:setUpdateOrder($automator,"true"),
  
    if ($error) then(
      context:failOrder($context),
      context:completeTaskOnExit($context,"failure")
    ) else (
     context:completeTaskOnExit($context,"success")
   
    )
    ,
    (
        <OrderDataUpdate xmlns="http://www.metasolv.com/OMS/OrderDataUpdate/2002/10/25">
            <UpdatedNodes>
                <_root>
                    <Status>
                     <ProvisioningDSLRegion2Status>
                    <ErrorCode>{$code}</ErrorCode>
                    <ErrorStatus>{$status}</ErrorStatus>
                    <ErrorMessage>{$message}</ErrorMessage>
                    </ProvisioningDSLRegion2Status>
                 </Status>
                   
                </_root>
            </UpdatedNodes>
            {
                for $orderComponentItem in $component/oms:orderItem
                let $index := fn:data($orderComponentItem/@index)
                let $path := fn:concat("/ControlData/Functions/ProvisioningFunction/orderItem[@index='",$index,"']")
                return
                    <Add path="{$path}">
                       <ExternalFulfillmentState>{$responseRoot/res:status/text()}</ExternalFulfillmentState>
                    </Add>    
            }
        </OrderDataUpdate>
    )
)
