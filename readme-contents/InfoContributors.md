# Info Contributors

## What is provided?

A `ProductIdInfoContributor` component is included by default and can be excluded by the `hmpps.info.product-id-enabled`
property to `false`.

The component looks for the `product-id` spring property or `PRODUCT_ID` environment variable and includes the value in
a `productId` field in the `/info` endpoint output.  The `PRODUCT_ID` environment variable will be set when an
application is deployed using the generic service helm chart with a `productId` value set.  If it can't find the
property then a value of `default` will be output instead.

Note that if the application already includes an info contributor that sets the `productId` field then this library
version will have no impact.

## What can I customize?

Setting the `product-id` or `PRODUCT_ID` value will change the value in the `/info` endpoint.

## How do I opt out?

Set the `hmpps.info.product-id-enabled` property to `false`.

## Real World Examples

[Search Github for real world examples](https://github.com/search?q=org%3Aministryofjustice+productId%3A+values.yaml&type=code).
