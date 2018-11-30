declare namespace oms="urn:com:metasolv:oms:xmlapi:1";
declare namespace saxon="http://saxon.sf.net/";
declare namespace xsl="http://www.w3.org/1999/XSL/Transform";
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


(:
                    <ControlData>
                        <Functions>
                            <BillingFunction>
                                <componentKey>{$component/oms:componentKey/text()}</componentKey>
                                <Status>
                                    <ErrorCode>{$responseRoot/res:errorCode/text()}</ErrorCode>
                                    <ErrorMessage>{$responseRoot/res:message/text()}</ErrorMessage>
                                    <ErrorStatus>{$responseRoot/res:status/text()}</ErrorStatus>                                    
                                </Status>
                            </BillingFunction>
                        </Functions>
                    </ControlData>
                    
:)

let $update := <UpdateOrder.Request xmlns="urn:com:metasolv:oms:xmlapi:1">
                <OrderID>{ context:getOrderId($context) }</OrderID>
                <View>OsmCentralOMExampleQueryTask</View>
            <UpdatedNodes>
                <_root>
                    <Status>
                            <BillingStatus>
                                    <ErrorCode>{$responseRoot/res:errorCode/text()}</ErrorCode>
                                    <ErrorMessage>{$responseRoot/res:message/text()}</ErrorMessage>
                                    <ErrorStatus>{$responseRoot/res:status/text()}</ErrorStatus>                                    
                            </BillingStatus>
                     </Status>
                </_root>
            </UpdatedNodes>
            </UpdateOrder.Request>
let $requestStr := saxon:serialize($update, <xsl:output method='xml' omit-xml-declaration='yes' indent='yes' saxon:indent-spaces='4'/>)
return (
    log:info($log,'received billing response'),
    automator:setUpdateOrder($automator,"true"),
    context:completeTaskOnExit($context,"success"),
    (
      if (exists($cancel)) then (
         automator:setUpdateOrder($automator,"false"),
         log:info($log, fn:concat('BILLING UPDATE ORDER: ' , $requestStr)),
        let $ret :=  context:processXMLRequest($context, $requestStr)
        return 
        log:info($log, fn:concat('BILLING UPDATE ORDER RESPONSE: ' , $ret))
         
              
        ) else (
        <OrderDataUpdate xmlns="http://www.metasolv.com/OMS/OrderDataUpdate/2002/10/25">
            <UpdatedNodes>
                <_root>
                    <Status>
                            <BillingStatus>
                                    <ErrorCode>{$responseRoot/res:errorCode/text()}</ErrorCode>
                                    <ErrorMessage>{$responseRoot/res:message/text()}</ErrorMessage>
                                    <ErrorStatus>{$responseRoot/res:status/text()}</ErrorStatus>                                    
                            </BillingStatus>
                     </Status>
                </_root>
            </UpdatedNodes>
            {
                for $orderComponentItem in $component/oms:orderItem
                let $index := fn:data($orderComponentItem/@index)
                let $path := fn:concat("/ControlData/Functions/BillingFunction/orderItem[@index='",$index,"']")
                return
                    <Add path="{$path}">
                       <ExternalFulfillmentState>{$responseRoot/res:status/text()}</ExternalFulfillmentState>
                    </Add>    
            }
        </OrderDataUpdate>
    )
    )
)
