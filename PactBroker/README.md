# PACT-BROCKER CI WITH JENKINS

A continuación veremos los pasos para validar pactos de consumidor y proveedor en un workflow de integración continua con Jenkins.

## PASO PREVIOS

Disponer de un API-Token en Jenkins para un usuario.

Pasos para crear un API-TOKEN.

- Hacer click en el nombre de usuario de Jenkins (arriba a la derecha)
- En el menú izquierdo ir a "Configurar"
- Añadir un nuevo API TOKEN
- Copiar el valor
- Para poder realizar llamadas POST desde fuera, lo más cómodo es codificar la clave en Base64 y pasarla como cabecera de autenticación

  - Ir a <https://www.base64encode.org/>
  - Introducir: `nombreUsuarioJenkins:API-TOKEN`
    - Por ejemplo: `fran:11505d62fcdd42d3d7d645ee3f1414297c`
    - Guardar el código generado. Por ejemplo: `ZnJhbjoxMTUwNWQ2MmZjZGQ0MmQzZDdkNjQ1ZWUzZjE0MTQyOTdj`
  - Con esto, las llamadas que realicemos a nuestro Jenkins para lanzar los jobs deberán tener la cabecera:
    - `Authorizartion: Basic ZnJhbjoxMTUwNWQ2MmZjZGQ0MmQzZDdkNjQ1ZWUzZjE0MTQyOTdj`

## CONFIGURAR WORKFLOW

Los siguientes pasos, podría tener sentido realizarlos en dos instancias de Jenkins independientes, nosotros lo haremos sobre la misma por simplificar el proceso.

### Configurar Jobs de publicación de pactos

- Configuración previa Pact Broker con docker
  - Para permitir la conexión entre el contenedor de docker y nuestro Jenkins local debemos añadir las siguientes líneas al archivo docker-compose
    - `PACT_BROKER_WEBHOOK_SCHEME_WHITELIST: http`
    - `PACT_BROKER_WEBHOOK_HOST_WHITELIST: 192.168.0.12`
- Iniciar Pact Broker: `docker-compose up`
- Arrancar Jenkins en local
- Crear una tarea de tipo "**Pipeline**"
  - En este caso la llamaremos: "Consumer-PubilshPacts"
- _Este paso es opcional_. Podríamos configurar esta tarea para que se ejecute cuando detecte cambios en el repositorio
  - Indicar el GitHub Project correspondiente
  - Configurar la periodicidad de consulta del repositorio
    - Por ejemplo, cada 5 minutos -> `*/5 * * * * `
- Crear el pipeline para creación y publicación de pactos

  - ```groovy
        pipeline {
        agent any

        stages {
            stage('Get project'){
                steps{
                   git branch: '6-PactBroker', url: 'https://github.com/morvader/Pact-Workshop'
                   bat "npm install"
                }
            }
            stage('Create Pacts') {
                steps {
                    bat "npm run generate-pact-client"
                }
            }
            stage('Publish Pacts') {
                steps {
                    bat "npm run publish-pacts-Broker"
                }
            }
        }
    }
    ```

- Con esto, ya deberíamos visualizar los pactos en PactBroker
  - <http://localhost:8000>

## CREATE WEBHOOK

```json
{
  "events": [
    {
      "name": "contract_published"
    }
    // {
    //   "name": "contract_content_changed"
    // }
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

## CAN I DEPLOY

### CHECK CLIENT

`npx pact-broker can-i-deploy --pacticipant FilmsClient --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest`

### CHECK PROVIDER

`npx pact-broker can-i-deploy --pacticipant FilmsProvider --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest`

### REFERENCES

<https://blog.testproject.io/2020/06/09/integrating-consumer-contract-testing-in-build-pipelines/>
<https://kreuzwerker.de/post/integrating-contract-tests-into-build-pipelines-with-pact-broker-and>

```

```
