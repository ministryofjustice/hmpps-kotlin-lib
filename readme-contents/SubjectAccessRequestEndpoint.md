# Subject Access Request Endpoints

## What is provided?

If you implement one of the three subject access request interfaces then 2 `/subject-access-request` endpoints will
be automatically created and call your service:

### Endpoints:
- GET `/subject-access-request` this endpoint should return data held by your service for the specified identifier 
within the provided date range.
- GET `/subject-access-request/template` should return your service's subject access request report mustache 
template file. This template is used to format your service's data in the generated Subject Access Request report PDF.

The endpoint will be protected by a `SAR_DATA_ACCESS` role and
an additional role can be added if required.

If you do not implement the service then these endpoints will not be created.

## What can I customize?
In terms of service implementations there are options:

1. For applications that *only* store prisoner information implement `HmppsPrisonSubjectAccessRequestService`.
If the subject access request endpoint is called with a prison number then your service will be called. 
1. For applications that *only* store probation information implement `HmppsProbationSubjectAccessRequestService`.
If the subject access request endpoint is called with a case reference number then your service will be called.
1. For applications that store prisoner and also probation information then implement `HmppsPrisonProbationSubjectAccessRequestService`.
If either a prison number or case reference number is supplied to the endpoint then your service will be called.

The default controller endpoint will be protected by `SAR_DATA_ACCESS`.  Specifying a `hmpps.sar.additionalAccessRole`
property in `application.yml` will then add in the additional access role as well.

## Subject Access Request Template configuration
To configure you service subject access request report template:

- Create a `template.mustache` file in your project `resources` dir. There is no mandatory directory structure or 
naming/versioning convention for the template file. Use whatever approach best aligns with your project structure/style. 
The only **mandatory requirement** is the template file must accessible as a resource at runtime.


- Add the following to your application properties (update as required): 
    ```yaml
    subject-access-request:
      template-path: /path_to_your_template/you_template.mustache
    ```

- If correctly configure calling GET `/subject-access-request/template` on your service will return the template body.

To use a different templates per environment simply create a new template file in your project resources directory and 
override the `subject-access-request.template-path` property in the target environment's configuration.


### Troubleshooting
- Endpoint will return status **500** if the `subject-access-request.template-path` property has not been set/is blank
- Endpoint will return status **404** if the configured template file is not found.

## How do I opt out?

If you do not implement the service interface then no endpoint will be created.

## Testing your service
See the `SubjectAccessRequestServiceSampleTest` and `SubjectAccessRequestSampleIntegrationTest` tests in the subproject
`test-app` package for sample tests to ensure your service and endpoint is working as expected.

## Real World Examples

[Search Github for real world examples](https://github.com/search?q=org%3Aministryofjustice+uk.gov.justice.hmpps.kotlin.sar&type=code).
