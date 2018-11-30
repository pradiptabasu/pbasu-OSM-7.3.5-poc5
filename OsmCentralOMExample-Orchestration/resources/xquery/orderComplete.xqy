declare namespace osm="urn:com:metasolv:oms:xmlapi:1";
let $order := ..//osm:GetOrder.Response

return
<requestResponse>
	<numSalesOrder>{$order/osm:Reference/text()}</numSalesOrder>
	<numOrder>{$order/osm:OrderID/text()}</numOrder>
	<typeOrder>{$order//osm:OrderHeader/osm:typeOrder/text()}</typeOrder>
	<errorCode>0</errorCode>
	<status>OK</status>
	<message>Order {$order/osm:Reference/text()} Complete</message>
</requestResponse>