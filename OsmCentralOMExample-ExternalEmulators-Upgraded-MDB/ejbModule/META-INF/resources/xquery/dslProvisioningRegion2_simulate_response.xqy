(: only require to be declared when editing with Oxygen :)
declare namespace log = "java:org.apache.commons.logging.Log";
declare namespace logFactory = "java:org.apache.commons.logging.LogFactory";
declare namespace java="http://xml.apache.org/xslt/java";

declare namespace req = "http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/dsl_region2";
declare namespace oi = "http://xmlns.oracle.com/InputMessage";
(: declare references to external objects we will use to process this transformation :) 
declare variable $log external;  

(: declare some variables pointing to relevant parts of the order for our transformation :)
let $order := fn:root()/req:activationOrderAdsl/req:order
let $location := $order/req:customerAccount/req:customerAddress/oi:city/text()
let $typeAdsl := $order/req:typeADSL/text()
let $error := fn:not($location = 'Rio de Janeiro') and $typeAdsl = 'DSL_8M'


return
(
<orderResponse xmlns="http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/interactionResponse"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <numSalesOrder>{$order/req:numSalesOrder/text()}</numSalesOrder>
    <numOrder>{$order/req:numOrder/text()}</numOrder>
    <typeOrder>{$order/req:typeOrder/text()}</typeOrder>
    <errorCode>{if ($error) then (
        '1'
    ) else ('0')}</errorCode>
    <!--<central>ABC12</central>
    <port>34AB</port>-->
    <message>{if ($error) then (
        'Not enough speed for DSL 8M.'
    ) else ('OK')}</message>
    <status>Provisioning_Completed</status>
</orderResponse>

)