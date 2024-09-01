# auth
This application leverages the Backend for Frontend (BFF) pattern in alongside with Single Sign-On (SSO) via Keycloak to securely access resources.

In this setup, the end user does not directly handle authentication tokens. Instead, the user is authenticated using an authorization code obtained from Keycloak. The BFF layer establishes a session based on this authorization code and securely manages the tokens. The BFF server then uses the valid token to access resources from the resource server on behalf of the user and returns the relevant data to the frontend.

This ensures a secure flow where sensitive tokens are not exposed to the frontend, and all communication with resource servers is handled server-side by the BFF..

~~~mermaid
sequenceDiagram
    participant User
    participant Keycloak
    participant bff
    participant Resourcer-server
    User->>bff: 1 access to resource
    bff->>User: 1 redirect user to keycloak login page
    Keycloak->>User: 1 login page
    Keycloak->>User: 2 Once user authenticated, the authorization-server redirects the user back to the client with athorization_code
    User->>bff: 2 send request to access resource with the athotization code
    bff --> Keycloak: 2 check if the athorization code is valid and get token
    bff --> bff: 2 Saved SecurityContext
    bff ->> Resourcer-server : 3 Request the resource with the token
    Resourcer-server-->Keycloak : 3 validate the token with keycloak
    Resourcer-server ->> bff : 4  responceing the request to bff to send it to user 
    bff ->> User: 4 send respoce from resource server back to user.

~~~
