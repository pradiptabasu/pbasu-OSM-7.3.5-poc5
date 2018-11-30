declare namespace osm="urn:com:metasolv:oms:xmlapi:1";
declare namespace log = "java:org.apache.commons.logging.Log";

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
<requestResponse>
	<numSalesOrder>{$order/osm:Reference/text()}</numSalesOrder>
	<numOrder>{$order/osm:OrderID/text()}</numOrder>
	<typeOrder>{$order//osm:OrderHeader/osm:typeOrder/text()}</typeOrder>
	<errorCode>0</errorCode>
	<status>{$mappedUpstreamFulfillmentState}</status>
	<message>Order {$order/osm:Reference/text()} Complete</message>
	{
	    for $orderItem in $order/osm:_root/osm:ControlData/osm:OrderItem
	    where exists($orderItem/osm:OrderItemFulfillmentState)
	    return 
	        <OrderItem>
	            <Name>{$orderItem/osm:lineItemName/text()}</Name>
	            <Status>{local:getUpstreamOrderItemFulfillmentState($orderItem/osm:OrderItemFulfillmentState/text())}</Status>
	        </OrderItem>
    }
</requestResponse>
)