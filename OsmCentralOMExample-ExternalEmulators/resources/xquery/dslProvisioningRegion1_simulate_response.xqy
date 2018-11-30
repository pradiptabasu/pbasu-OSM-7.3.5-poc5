(: only require to be declared when editing with Oxygen :)
declare namespace log = "java:org.apache.commons.logging.Log";

declare namespace req = "http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/dsl_region1";
declare namespace oi = "http://xmlns.oracle.com/InputMessage";
(: declare references to external objects we will use to process this transformation :) 
declare variable $log external;  

(: declare some variables pointing to relevant parts of the order for our transformation :)

let $order := fn:root()/req:activationOrderAdsl/req:order
let $cancel := fn:root()/req:cancelOrderAdsl/req:order
let $location := $order/req:customerAccount/req:customerAddress/oi:city/text()
let $typeAdsl := $order/req:typeADSL/text()
let $error := fn:not($location = 'Rio de Janeiro') and $typeAdsl = 'DSL_8M'

return
(

if (exists($cancel)) then (
log:info($log,'creating DSL Region 1 cancel response'),
<cancelResponse xmlns="http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/interactionResponse"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <numSalesOrder>{$cancel/req:numSalesOrder/text()}</numSalesOrder>
    <numOrder>{$cancel/req:numOrder/text()}</numOrder>
    <typeOrder>{$cancel/req:typeOrder/text()}</typeOrder>
    <errorCode>0</errorCode>
    <message>CANCEL</message>
    <status>C</status>
</cancelResponse>
) else (
log:info($log,'creating DSL Region 1 response'),
<orderResponse xmlns="http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/interactionResponse"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <numSalesOrder>{$order/req:numSalesOrder/text()}</numSalesOrder>
    <numOrder>{$order/req:numOrder/text()}</numOrder>
    <typeOrder>{$order/req:typeOrder/text()}</typeOrder>
    <errorCode>{if ($error) then (
        '1'
    ) else ('0')}</errorCode>
    <message>{if ($error) then (
        'Not enough speed for DSL 8M.'
    ) else ('OK')}</message>
    <status>Provisioning_Completed</status>
</orderResponse>
)




)