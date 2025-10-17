# Subject Access Request Endpoints

## What is provided?

If you implement one of the three subject access request interfaces the `/subject-access-request` endpoint will
be automatically created in your service:

### Endpoints:
- `/subject-access-request` this endpoint should return SAR data held by your service for the specified identifier 
within the provided date range.
- Additionally you can configure your application to create the `/subject-access-request/template` endpoint. See
[Subject Access Request Template Endpoint Configuration](#subject-access-request-template-endpoint-configuration) for 
details

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

## Subject Access Request Template Endpoint Configuration
The subject access request service uses mustache templates to convert raw API data into a human-readable format when 
generating the report PDFs. These templates currently live in the SAR service however, the long term goal is to move 
these templates into the service repositories where they will be owned and maintained by the service teams. The SAR 
service will use the `/subject-access-request/template` endpoint to retrieve the templates at runtime. The template 
endpoint is currently disabled by default to avoid introducing a breaking change and to allow teams some flexibility 
around when they move to this model. 

To enable and register the subject access request template endpoint:

- Ensure your service is implementing one of the HMPPS Subject Access Request service interfaces:
  - `HmppsSubjectAccessRequestReactiveService`
  - `HmppsSubjectAccessRequestService`


- Create a `sar_template.mustache` file under your project `resources` dir (name as desired). There is no mandatory
  directory structure or naming/versioning convention for the template file. The only **mandatory requirement** is the
  template file must be accessible as a resource at runtime.

- Add the following to your application properties (update as required):
    ```yaml
    hmpps:
      sar:
        template:
          path: /path_to_template/sar_template.mustache
          enabled: true
    ```

If correctly configure calling GET `/subject-access-request/template` on your service will return the template body. The
application will fail on start up if: 
- The feature is enabled and no template path is configured.
- The feature is enabled and the template path is invalid/file does not exist.


To use a different version of your template between environments simply create a new template file in your project 
resources directory and override the `hmpps.sar.template.path` property in the target environment's configuration. This 
gives you the flexibility to test work-in-progress templates locally/in the Dev environment without impacting the live 
production template.

## How do I opt out?

If you do not implement the service interface then no endpoint will be created.

## Testing your service
See the `SubjectAccessRequestServiceSampleTest` and `SubjectAccessRequestSampleIntegrationTest` tests in the subproject
`test-app` package for sample tests to ensure your service and endpoint is working as expected.

## Real World Examples

[Search Github for real world examples](https://github.com/search?q=org%3Aministryofjustice+uk.gov.justice.hmpps.kotlin.sar&type=code).
