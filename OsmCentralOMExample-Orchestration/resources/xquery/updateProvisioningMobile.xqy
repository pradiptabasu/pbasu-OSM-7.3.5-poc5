declare namespace osm="urn:com:metasolv:oms:xmlapi:1";
declare namespace context = "java:com.mslv.oms.automation.OrderDataChangeNotificationContext";
declare namespace log = "java:org.apache.commons.logging.Log";
declare namespace saxon="http://saxon.sf.net/";
declare namespace xsl="http://www.w3.org/1999/XSL/Transform";

declare variable $context external;
declare variable $log external;

let $order := ..//osm:GetOrder.Response
let $status := $order/osm:_root/osm:Status/osm:ProvisioningMobileStatus
let $requestStr := saxon:serialize(fn:root(), <xsl:output method='xml' omit-xml-declaration='yes' indent='yes' saxon:indent-spaces='4'/>)

return
(
log:info($log,'update provisioning mobile status'),
(:log:info($log,fn:concat('inputXML:', $requestStr)),
log:info($log,fn:concat('functions:', $functions)),:)
<requestResponse>
	<numSalesOrder>{$order/osm:Reference/text()}</numSalesOrder>
	<numOrder>{$order/osm:OrderID/text()}</numOrder>
	<serviceType>Mobile</serviceType>
	<typeOrder>{$order//osm:OrderHeader/osm:typeOrder/text()}</typeOrder>
	<errorCode>{$status/osm:ErrorCode/text()}</errorCode>
	<status>{$status/osm:ErrorStatus/text()}</status>
	<message>{$status/osm:ErrorMessage/text()}</message>
</requestResponse>

)