declare namespace osm="urn:com:metasolv:oms:xmlapi:1";
declare namespace log = "java:org.apache.commons.logging.Log"; 

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
  
  <requestResponse>
	  <numSalesOrder>{$order/osm:Reference/text()}</numSalesOrder>
    <salesOrderRevision>{$salesOrderRevision}</salesOrderRevision>
	  <numOrder>{$order/osm:OrderID/text()}</numOrder>
	  <typeOrder>{$order//osm:OrderHeader/osm:typeOrder/text()}</typeOrder>
	  <errorCode>00</errorCode>
	  <status>OK</status>
	  <message>Order {$order/osm:Reference/text()} Complete</message>
  </requestResponse>
)