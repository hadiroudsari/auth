# auth
auth

~~~mermaid
sequenceDiagram
    participant User
    participant Keycloak
    participant bff
    participant Resourcer-server
    User->>bff: 1 access to resource
    bff ->> User: 1 redirect to keycloak login page
    User ->> Keycloak: 1 get request for login page
    Keycloak ->> User: 1 login page     

~~~
