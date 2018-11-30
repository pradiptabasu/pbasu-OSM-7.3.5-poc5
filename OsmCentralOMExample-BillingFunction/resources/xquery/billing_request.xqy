(: only require to be declared when editing with Oxygen :)
declare namespace automator = "java:oracle.communications.ordermanagement.automation.plugin.ScriptSenderContextInvocation";
(: only require to be declared when editing with Oxygen :)
declare namespace context = "java:com.mslv.oms.automation.TaskContext";
(: only require to be declared when editing with Oxygen :)
declare namespace log = "java:org.apache.commons.logging.Log";
declare namespace oms="urn:com:metasolv:oms:xmlapi:1";
declare namespace im="http://xmlns.oracle.com/InputMessage";

 
declare variable $automator external; 
declare variable $context external;
declare variable $log external;  

let $order := /oms:GetOrder.Response
let $customer := $order/oms:_root/oms:CustomerDetails
let $account := $order/oms:_root/oms:AccountDetails
let $header := $order/oms:_root/oms:OrderHeader
let $billingProfile := $order/oms:_root/oms:BillingProfile

return
(

log:info($log,fn:concat('billing request[',/oms:GetOrder.Response/oms:OrderID,'] count[',count(/oms:GetOrder.Response/oms:_root/oms:data),']')),
<ns1:configureBilling xmlns:ns1="http://xmlns.oracle.com/communications/sce/dictionary/OsmCentralOMExample/billing"
 xmlns:ns2="http://xmlns.oracle.com/InputMessage"
 xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <ns1:order>
        <ns1:numSalesOrder>{$header/oms:numSalesOrder/text()}</ns1:numSalesOrder>
        <ns1:numOrder>{fn:concat($order/oms:OrderID,'-',$order/oms:OrderHistID)}</ns1:numOrder>
        <ns1:typeOrder>{$header/oms:typeOrder/text()}</ns1:typeOrder>
        <ns1:commandProduct>4544</ns1:commandProduct>
        <ns1:invoiceProfile>
                <ns2:mediaType>{$billingProfile/oms:mediaType/text()}</ns2:mediaType>
                <ns2:typeInvoice>{$billingProfile/oms:typeInvoice/text()}</ns2:typeInvoice>
                <ns2:billingCycle>{$billingProfile/oms:billingCycle/text()}</ns2:billingCycle>
                <ns2:exemptionICMS>{$billingProfile/oms:exemptionICMS/text()}</ns2:exemptionICMS>
                <ns2:empresaFaturamento>{$billingProfile/oms:empresaFaturamento/text()}</ns2:empresaFaturamento>
                <ns2:paymentMethod>{$billingProfile/oms:paymentMethod/text()}</ns2:paymentMethod>
                <ns1:idInvoiceProfile>TODO</ns1:idInvoiceProfile>
                <ns1:numCustomerAccount>{$account/oms:numAccount/text()}</ns1:numCustomerAccount>
                <ns1:invoiceProfileAddress>
                    <ns2:locationType>{$customer/oms:locationType/text()}</ns2:locationType>
                    <ns2:nameLocation>{$customer/oms:nameLocation/text()}</ns2:nameLocation>
                    <ns2:number>{$customer/oms:number/text()}</ns2:number>
                    <ns2:typeCompl>{$customer/oms:typeCompl/text()}</ns2:typeCompl>
                    <ns2:numCompl>{$customer/oms:numCompl/text()}</ns2:numCompl>
                    <ns2:district>{$customer/oms:district/text()}</ns2:district>
                    <ns2:codeLocation>{$customer/oms:codeLocation/text()}</ns2:codeLocation>
                    <ns2:city>{$customer/oms:city/text()}</ns2:city>
                    <ns2:state>{$customer/oms:state/text()}</ns2:state>
                    <ns2:referencePoint>{$customer/oms:referencePoint/text()}</ns2:referencePoint>
                    <ns2:areaCode>{$customer/oms:areaCode/text()}</ns2:areaCode>
                    <ns2:typeAddress>{$customer/oms:typeAddress/text()}</ns2:typeAddress>
                </ns1:invoiceProfileAddress>
        </ns1:invoiceProfile>
    </ns1:order>
</ns1:configureBilling>
)