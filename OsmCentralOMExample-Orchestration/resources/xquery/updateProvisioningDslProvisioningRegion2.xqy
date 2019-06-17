declare namespace osm="urn:com:metasolv:oms:xmlapi:1";
declare namespace im="http://xmlns.oracle.com/MilestoneMessage";

declare namespace context = "java:com.mslv.oms.automation.OrderDataChangeNotificationContext";
declare namespace log = "java:org.apache.commons.logging.Log";
declare namespace saxon="http://saxon.sf.net/";
declare namespace xsl="http://www.w3.org/1999/XSL/Transform";

declare variable $context external;
declare variable $log external;

let $order := ..//osm:GetOrder.Response
let $status := $order/osm:_root/osm:Status/osm:ProvisioningDSLRegion2Status
let $requestStr := saxon:serialize(fn:root(), <xsl:output method='xml' omit-xml-declaration='yes' indent='yes' saxon:indent-spaces='4'/>)

return
(
log:info($log,'update DSL Region 2 provisioning status'),
(:log:info($log,fn:concat('inputXML:', $requestStr)),
log:info($log,fn:concat('functions:', $functions)),:)
<im:requestResponse xmlns:im="http://xmlns.oracle.com/MilestoneMessage">
	<im:numSalesOrder>{$order/osm:Reference/text()}</im:numSalesOrder>
	<im:numOrder>{$order/osm:OrderID/text()}</im:numOrder>
	<im:serviceType>ADSL</im:serviceType>
	<im:typeOrder>{$order//osm:OrderHeader/osm:typeOrder/text()}</im:typeOrder>
	<im:errorCode>{$status/osm:ErrorCode/text()}</im:errorCode>
	<im:status>{$status/osm:ErrorStatus/text()}</im:status>
	<im:message>{$status/osm:ErrorMessage/text()}</im:message>
</im:requestResponse>

)