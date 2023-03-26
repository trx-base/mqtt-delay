# About mqtt-delay

This microservice does one thing well:

1. Delays an MQTT message for a given time period.

## Development Rules

1. Don't solve problems you don't have
2. Commit often
3. Build locally before push
4. Push often
5. 100% Test Coverage

## Docker Compose

A working example of a **docker-compose.yml** can be found in folder docker-compose.

- Rename **.env.example** to **.env** and adapt to your needs.
- Rename **application.yml.example** to **application.yml** and adapt configuration

### Traefik

The provided docker-compose.yml is already preconfigured to work with Traefik reverse proxy. An already preconfigured
and ready to go Traefik docker-compose configuration can be found
here: https://github.com/trx-base/traefik-docker-compose

## Build

* **Build**: `./gradlew clean build`
* **Code coverage report**: `./gradlew koverReport` - Report can be found at `build/reports/kover/html/index.html`

## Subscriber

### delayed/{period}/{topic}

Delays a message to a {topic } for a given {period} in seconds.

**Example**

* Topic: `delaye/5/light/activate`

*...will delay the message to `light/activate` for 5 seconds.*

## Test Cases

Each separate test case begins at its own hour (e.g 03:00:00). These tests are implemented
in `integration.AcceptanceIntegrationTest`

| Subscribed     | Publish | Payload | hh:mm:ss |
|----------------|---------|:--------|----------|
| delay/5/topic1 |         |         | 00:00:00 |
|                | topic1  |         | 00:00:05 |
| delay/2/topic1 |         |         | 00:01:00 |
|                | topic1  |         | 00:01:02 |
| delay/5/topic1 |         |         | 01:00:00 |
| delay/5/topic1 |         |         | 01:00:00 |
|                | topic1  |         | 01:00:05 |
| delay/5/topic1 |         |         | 02:00:00 |
| delay/5/topic1 |         |         | 02:00:02 |
| delay/5/topic1 |         |         | 02:00:04 |
|                | topic1  |         | 02:00:05 |
| delay/5/topic1 |         |         | 03:00:00 |
| delay/5/topic1 |         |         | 03:00:01 |
| delay/5/topic1 |         |         | 03:00:06 |
|                | topic1  |         | 03:00:05 |
|                | topic1  |         | 03:00:11 |
| delay/5/topic1 |         |         | 04:00:00 |
| delay/5/topic2 |         |         | 04:00:00 |
|                | topic1  |         | 04:00:05 |
|                | topic2  |         | 04:00:05 |

## Future Outlook

Potential features that may be implemented in future (incomplete list):

* Multi - delay is not reset when multiple messages received for a specific topic, but all executed individually.
* Extend - delay period is extended by the period amount for each received message of a specific topic. 