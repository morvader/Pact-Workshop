# PACT-BROCKER CI WITH JENKINS

A continuación veremos los pasos para validar pactos de consumidor y proveedor en un workflow de integración continua con Jenkins.

## PASO PREVIOS

Disponer de un API-Token en Jenkins para un usuario.

Pasos para crear un API-TOKEN.

1. Hacer click en el nombre de usuario de Jenkins (arriba a la derecha)
2. En el menú izquierdo ir a "Configurar"
3. Añadir un nuevo API TOKEN
4. Copiar el valor
5. Para poder realizar llamadas POST desde fuera, lo más cómodo es codificar la clave en Base64 y pasarla como cabecera de autenticación
    - Ir a <https://www.base64encode.org/>
    - Introducir: `nombreUsuarioJenkins:API-TOKEN`
       - Por ejemplo: `fran:11505d62fcdd42d3d7d645ee3f1414297c`
    - Guardar el código generado. Por ejemplo: `ZnJhbjoxMWRlZWE5ODBiNmMxZmJkZWYxMjRlZGQ0ZWY3NjhkZWMx`
  
6. Con esto, las llamadas que realicemos a nuestro Jenkins para lanzar los jobs deberán tener la cabecera:
    - `Authorizartion: Basic ZnJhbjoxMWRlZWE5ODBiNmMxZmJkZWYxMjRlZGQ0ZWY3NjhkZWMx`

## CONFIGURAR WORKFLOW

Los siguientes pasos, podría tener sentido realizarlos en dos instancias de Jenkins independientes, nosotros lo haremos sobre la misma por simplificar el proceso.

1. Configuración previa Pact Broker con docker
     - Para permitir la conexión entre el contenedor de docker y nuestro Jenkins local debemos añadir las siguientes líneas al archivo docker-compose
       - `PACT_BROKER_WEBHOOK_SCHEME_WHITELIST: http`
       - `PACT_BROKER_WEBHOOK_HOST_WHITELIST: 192.168.0.12`
2. Iniciar Pact Broker: `docker-compose up`

### JOB - CONSUMER: Publicación de pactos

1. Ir a Jenkins
2. Crear una tarea de tipo "**Pipeline**"
     - En este caso la llamaremos: "PublishPacts"
3. _Este paso es opcional_. Podríamos configurar esta tarea para que se ejecute cuando detecte cambios en el repositorio
     - Indicar el GitHub Project correspondiente
     - Configurar la periodicidad de consulta del repositorio
       - Por ejemplo, cada 5 minutos -> `*/5 * * * * `
4. Crear el pipeline para creación y publicación de pactos

  ```groovy
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

5. Con esto, ya deberíamos visualizar los pactos en PactBroker
    - <http://localhost:8000>

### JOB - PROVIDER: Verificar pactos

1. Ir a Jenkins
2. Crear una tarea de tipo "**Pipeline**"
    - En este caso la llamaremos: "VerifyPacts"
3. Este pipeline realizará varias tareas:
    - Verificar que el pactos se cumplen
    - Publicar los resultados de la verificación en el Pact Broker
    - Llamar al comando de Pact "**can i deploy**" para saber si es seguro desplegar el servidor
4. Pipeline:
  
```groovy
pipeline {
    agent any
    stages {
        stage('Get project') {
            steps {
                git branch: '6-PactBroker', url: 'https://github.com/morvader/Pact-Workshop'
                script {
                    if (isUnix()) {
                        sh 'npm install'
                    }
                    else {
                        bat 'npm install'
                    }
                }
            }
        }
        stage('Create Pacts') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'npm run pact-server'
                    }
                    else {
                        bat 'npm run pact-server'
                    }
                }
            }
        }
        stage('Can-I-Deploy Server') {
            steps {
                script {
                    if (isUnix()) {
                        sh 'npx pact-broker can-i-deploy --pacticipant FilmsProvider --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest'
                    }
                    else {
                        bat 'npx pact-broker can-i-deploy --pacticipant FilmsProvider --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest'
                    }
                }
            }
        }
    }
}
```

- Lo deseable es que este paso se ejecute automáticamente cada vez que los pactos se modifiquen, para ello, en el paso siguiente veremos cómo crear un Webhook desde Pact Broker
- Para ello será necesario que en la configuración de esta tarea marquemos el check de que permite **ejecuciones remotas**.

### JOB - CONSUMER: Publicar consumidor

Una vez que tanto proveedor como consumidor hayan publicado y verificado los pactos, como último paso del proceso, debemos verificar que es seguro desplegar el consumir mediante la herramienta "**can-i-deploy**".

Para ello, en Jenkins crearemos otro proceso que sea llamado desde el Pact Broker cada vez que el proveedor verifique los pactos.

Paso a seguir:
1. Crear tarea en Jenkins de tipo "Pipeline"
     - En este caso la llamaremos: "Deploy Consumer"
2. Permitir que la tarea pueda ser ejecutada remotamente marcando el check correspondiente.
3. Pipeline:

```groovy
pipeline {
    agent any
    stages {
        stage('Can-I-Deploy Consumer') {
            steps {
                dir('../PublishPacts'){
                script {
                    if (isUnix()) {
                        sh 'npx pact-broker can-i-deploy --pacticipant FilmsClient --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest'
                    }
                    else {
                        bat 'npx pact-broker can-i-deploy --pacticipant FilmsClient --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest'
                    }
                }
                }
            }
        }
    }
}
```

## CREAR WEBHOOKS

Una vez que tenemos los pactos subidos, vamos a crear dos webhooks en Pact Broker. Uno de ellos para que el proveedor pueda verificar la validez de los contratos cada vez que haya algún cambio en los contratos y otro para que el proveedor sepa si pueda desplegarse una vez que el proveedor publique los resultados.

Creados dos webhooks distintos puesto, que cada una estas tareas se crea en tareas de jenkins separadas.

### Verificación del proveedor

Desde el Pact Broker

1. Acceder a <http://localhost:8000>
2. Si el paso anterior de publicación ha funcionado correctamente, deberían mostrarse los pactos publicados
3. En la columna de "*Webhook status*" pulsar en "**Create**"
4. En la siguiente vista, en la fila de "*pb:create*", en la columna "*NON-GET*" pulsar en el símbolo "**!**"
5. Se mostrar una ventana para introducir los valores de una petición *POST* para crear el webhook. En ella, introducir el siguiente BODY:

```json
{
  //Eventos que harán que el webhook se ejecute
  //Para la demo, se ejecutará cada vez que se publique el contrato. En un entorno real, lo habitual sería lanzarlo cada vez que cambien los contratos
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
    //URL del job del jenkins
    "url": "http://192.168.0.12:8080/job/VerifyPacts/build",
    "headers": {
      // Autorización anteriormente creada y codificada en base64
      "authorization": "Basic ZnJhbjoxMWRlZWE5ODBiNmMxZmJkZWYxMjRlZGQ0ZWY3NjhkZWMx"
    }
  }
}
```

### ¿Consumidor deplegable?

Creamos otro webhook para que el consumidor sepa si puede desplegarse una vez que el proveedor publique los resultados.

Para ello, creamos un webhook de la misma manera que el anterior pero con el siguiente BODY:

```json
{
  "events": [
    {
      "name": "provider_verification_published"
    }
  ],
  "request": {
    "method": "POST",
    "url": "http://192.168.0.12:8080/job/DeployConsumer/build",
    "headers": {
      "authorization": "Basic ZnJhbjoxMWRlZWE5ODBiNmMxZmJkZWYxMjRlZGQ0ZWY3NjhkZWMx"
    }
  }
}
```

## CAN I DEPLOY

Esta utilidad, incluida en la instalación de básica de Pact, indicará cuándo es seguro desplegar alguna de las partes que formen parte de los pactos albergados en Pact Broker.

Se podrían dar la siguiente casuísticas:

- Pactos publicados por el consumidor pero el consumidor aún no los ha verificado: FALSO. Motivo: No se puede determinar
- Pactos publicados por el consumidor pero fallan al ser verificados por el proveedor: FALSO. Motivo: Fallo en verificación
- Pactos publicados por el consumidor y el proveedor verifica que son correctos: OK. Motivo: Es seguro desplegar

Estos comandos podrían lanzarse desde línea de comandos en cualquier momento.

### CHECK CLIENT

`npx pact-broker can-i-deploy --pacticipant FilmsClient --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest`

### CHECK PROVIDER

`npx pact-broker can-i-deploy --pacticipant FilmsProvider --broker-base-url http://localhost:8000 --broker-username pact_workshop --broker-password pact_workshop --latest`

### REFERENCES
<https://docs.pact.io/pact_broker/webhooks>

<https://blog.testproject.io/2020/06/09/integrating-consumer-contract-testing-in-build-pipelines/>

<https://kreuzwerker.de/post/integrating-contract-tests-into-build-pipelines-with-pact-broker-and>
