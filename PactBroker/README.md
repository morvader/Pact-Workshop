# CAN I DEPLOY

## CREATE WEBHOOK

```json
{
  "events": [
    {
      "name": "contract_published"
    },
    {
      "name": "provider_verification_published"
    }
  ],
  "request": {
    "method": "POST",
    "url": "http://192.168.0.12:8080/job/Pact/build",
    "headers": {
      "authorization": "Basic ZnJhbjoxMTg2NGJjZDVhYTQ3YTg3MzdhNjkwNzNkOGVhZDhiZmMw"
    }
  }
}
```

## CHECK CLIENT

`npx pact-broker can-i-deploy --pacticipant FilmsClient --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest`

## CHECK PROVIDER

`npx pact-broker can-i-deploy --pacticipant FilmsProvider --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest`

### REFERENCES

<https://blog.testproject.io/2020/06/09/integrating-consumer-contract-testing-in-build-pipelines/>
<https://kreuzwerker.de/post/integrating-contract-tests-into-build-pipelines-with-pact-broker-and>

```

```
