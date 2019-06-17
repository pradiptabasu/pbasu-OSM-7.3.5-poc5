declare namespace osm="urn:com:metasolv:oms:xmlapi:1";
declare namespace log = "java:org.apache.commons.logging.Log";
declare namespace im="http://xmlns.oracle.com/MilestoneMessage";

declare variable $log external;

(: 
   This function is for indication purposes only.
   OSM Fulfillment State can be mapped according the expectation of Upstream 
:)
declare function local:getUpstreamFulfillmentState($fulfillmentState as xs:string) as xs:string {
    fn:concat('Order_Upstream_' , $fulfillmentState)
};

(: 
   This function is for indication purposes only.
   OSM Fulfillment State can be mapped according the expectation of Upstream 
:)
declare function local:getUpstreamOrderItemFulfillmentState($fulfillmentState as xs:string) as xs:string {
    fn:concat('OrderItem_Upstream_' , $fulfillmentState)
};

let $order := ..//osm:GetOrder.Response
let $orderFulfillmentState := $order/osm:_root/osm:ControlData/osm:OrderFulfillmentState
let $mappedUpstreamFulfillmentState := if(exists($orderFulfillmentState)) then local:getUpstreamFulfillmentState($orderFulfillmentState/text()) else ()

return
(
log:info($log,'Sending Upstream Fulfillment State'),
<im:requestResponse xmlns:im="http://xmlns.oracle.com/MilestoneMessage">
	<im:numSalesOrder>{$order/osm:Reference/text()}</im:numSalesOrder>
	<im:numOrder>{$order/osm:OrderID/text()}</im:numOrder>
	<im:typeOrder>{$order//osm:OrderHeader/osm:typeOrder/text()}</im:typeOrder>
	<im:errorCode>0</im:errorCode>
	<im:status>{$mappedUpstreamFulfillmentState}</im:status>
	<im:message>Order {$order/osm:Reference/text()} Complete</im:message>
	{
	    for $orderItem in $order/osm:_root/osm:ControlData/osm:OrderItem
	    where exists($orderItem/osm:OrderItemFulfillmentState)
	    return 
	        <im:OrderItem>
	            <im:Name>{$orderItem/osm:lineItemName/text()}</im:Name>
	            <im:Status>{local:getUpstreamOrderItemFulfillmentState($orderItem/osm:OrderItemFulfillmentState/text())}</im:Status>
	        </im:OrderItem>
    }
</im:requestResponse>
)