# Info Contributors

## What is provided?

By setting the `hmpps.info.product-id-enabled` property to `true` the `ProductIdInfoContributor` component is included.
The component looks for the `product-id` spring property or `PRODUCT_ID` environment variable and includes the value in
the `/info` endpoint output.  The `PRODUCT_ID` environment variable will be set when an application is deployed using 
the generic service helm chart with a `productId` value set.

## What can I customize?

Setting the `product-id` or `PRODUCT_ID` value will change the value in the `/info` endpoint.

## How do I opt out?

Don't set the `hmpps.info.product-id-enabled` property to `true`.
