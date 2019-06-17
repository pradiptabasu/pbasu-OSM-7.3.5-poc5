declare namespace osm="urn:com:metasolv:oms:xmlapi:1";
declare namespace log = "java:org.apache.commons.logging.Log"; 
declare namespace im="http://xmlns.oracle.com/MilestoneMessage";

(:below lines on xsl and saxon are required for printing XMLs:)
declare namespace xsl="http://www.w3.org/1999/XSL/Transform";
declare option saxon:output "method=xml";
declare option saxon:output "saxon:indent-spaces=2";

declare variable $log external; 

let $inputDoc := self::node()
let $order := fn:root(.)/osm:GetOrder.Response
let $salesOrderRevision := $order/osm:_root/osm:OrderHeader/osm:salesOrderRevision/text()

let $inputDocStr := if (fn:exists($inputDoc))
                	then
                		saxon:serialize($inputDoc, <xsl:output method='xml' omit-xml-declaration='yes' indent='yes' saxon:indent-spaces='4'/>)
                	else ""
                  
return (
  log:info($log,concat('numSalesOrder: ',$order/osm:Reference/text())), 
  log:info($log,concat('numOrder: ',$order/osm:OrderID/text())),
  log:info($log,concat('salesOrderRevision: ',$salesOrderRevision)), 
  
  (: Below prints only values and no tags. XML needs to be serialized before logging.
  log:info($log,concat('Order: ',$order)),
  :)
  (:Below prints the XML in full structure --->  inputDocStr serialized earlier
  log:info($log,concat('Input Doc: ',$inputDocStr)), 
  :)
  
  <im:requestResponse xmlns:im="http://xmlns.oracle.com/MilestoneMessage">
	  <im:numSalesOrder>{$order/osm:Reference/text()}</im:numSalesOrder>
	  <im:salesOrderRevision>{$salesOrderRevision}</im:salesOrderRevision>
	  <im:numOrder>{$order/osm:OrderID/text()}</im:numOrder>
	  <im:typeOrder>{$order//osm:OrderHeader/osm:typeOrder/text()}</im:typeOrder>
	  <im:errorCode>00</im:errorCode>
	  <im:status>Fulfillment Complete</im:status>
	  <im:message>Order {$order/osm:Reference/text()} Complete</im:message>
  </im:requestResponse>
)