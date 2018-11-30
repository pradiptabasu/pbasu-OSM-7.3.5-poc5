declare namespace im="http://xmlns.oracle.com/InputMessage";
let $order := ../im:order
let $customer :=  ../im:order/im:customerAccount
let $details := $customer/im:customerAddress
let $billing := $customer/im:invoiceProfile
return
<_root>
	<OrderHeader>
		<numSalesOrder>{$order/im:numSalesOrder/text()}</numSalesOrder>
		<typeOrder>{$order/im:typeOrder/text()}</typeOrder>
	</OrderHeader>
    <BillingProfile>
        <mediaType>{$billing/im:mediaType/text()}</mediaType>
        <typeInvoice>{$billing/im:typeInvoice/text()}</typeInvoice>
        <billingCycle>{$billing/im:billingCycle/text()}</billingCycle>
        <exemptionICMS>{if ($billing/im:exemptionICMS/text() = 'true') 
        					then
        						'Yes'
        					else
        						'No'}</exemptionICMS>
        <empresaFaturamento>{$billing/im:empresaFaturamento/text()}</empresaFaturamento>
        <paymentMethod>{$billing/im:paymentMethod/text()}</paymentMethod>
    </BillingProfile>
    <CustomerDetails>    	
        <locationType>{$details/im:locationType/text()}</locationType>
        <nameLocation>{$details/im:nameLocation/text()}</nameLocation>
        <number>{$details/im:number/text()}</number>
        <typeCompl>{$details/im:typeCompl/text()}</typeCompl>
        <numCompl>{$details/im:numCompl/text()}</numCompl>
        <district>{$details/im:district/text()}</district>
        <codeLocation>{$details/im:codeLocation/text()}</codeLocation>
        <city>{$details/im:city/text()}</city>
        <state>{$details/im:state/text()}</state>
        <referencePoint>{$details/im:referencePoint/text()}</referencePoint>
        <areaCode>{$details/im:areaCode/text()}</areaCode>
        <typeAddress>{$details/im:typeAddress/text()}</typeAddress>
       
    </CustomerDetails>
    <AccountDetails>
    	<numAccount>{$customer/im:numAccount/text()}</numAccount>
    	<status>{$customer/im:status/text()}</status>
    	<corporate>{$customer/im:corporate/text()}</corporate>
    	<cpfClient>{$customer/im:cpfClient/text()}</cpfClient>
    	<cnpjClient>{$customer/im:cnpjClient/text()}</cnpjClient>
    	<inscrState>{$customer/im:inscrState/text()}</inscrState>
    	<clientSince>{$customer/im:clientSince/text()}</clientSince>
    	<category>{$customer/im:category/text()}</category>
    </AccountDetails>

</_root>
