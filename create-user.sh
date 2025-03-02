#!/bin/bash

echo "Waiting for Keycloak to start..."
sleep 10  # Give Keycloak time to fully initialize

REALM_NAME="hadilka"

# Log in as the Keycloak admin
/opt/keycloak/bin/kcadm.sh config credentials --server http://localhost:8080 --realm master --user admin --password password


# Delete the realm if it exists
echo "Deleting existing realm '$REALM_NAME' (if any)..."
/opt/keycloak/bin/kcadm.sh delete realms/$REALM_NAME --server http://localhost:8080 || echo "Realm did not exist."

echo "Creating realm '$REALM_NAME'..."
/opt/keycloak/bin/kcadm.sh create realms -s realm=$REALM_NAME -s enabled=true

# First Client: `bff_cl` (Port 8082, PRIVATE)
CLIENT_ID1="bff_cl"
ROOT_URL1="http://localhost:8082"
VALID_REDIRECT_URIS1="http://localhost:8082/*"
VALID_POST_LOGOUT_REDIRECT_URIS1="http://localhost:8082/logout"
WEB_ORIGINS1="*"
CLIENT_SECRET="my-custom-secret"

# Second Client: `greeting` (Port 8081, PUBLIC)
CLIENT_ID2="greeting"
ROOT_URL2="http://localhost:8081"
VALID_REDIRECT_URIS2="http://localhost:8081/*"
VALID_POST_LOGOUT_REDIRECT_URIS2="http://localhost:8081/logout"
WEB_ORIGINS2="*"

USERNAME="testuser"
PASSWORD="testuser"
ROLE_USER="client_user"
ROLE_ADMIN="client_admin"


# Function to create a client
create_client() {
  CLIENT_ID=$1
  ROOT_URL=$2
  REDIRECT_URIS=$3
  LOGOUT_URIS=$4
  WEB_ORIGINS=$5
  IS_PUBLIC=$6  # Flag to determine if client should be public

  echo "Creating client '$CLIENT_ID'..."
  /opt/keycloak/bin/kcadm.sh create clients -r $REALM_NAME \
    -s clientId=$CLIENT_ID \
    -s enabled=true \
    -s publicClient=$IS_PUBLIC \
    -s directAccessGrantsEnabled=true \
    -s standardFlowEnabled=true \
    -s rootUrl=$ROOT_URL \
    -s baseUrl=$ROOT_URL \
    -s 'redirectUris=["'"$REDIRECT_URIS"'"]' \
    -s 'webOrigins=["'"$WEB_ORIGINS"'"]' \
    -s 'attributes."post.logout.redirect.uris"="'"$LOGOUT_URIS"'"'

}

# Create `bff_cl` (PRIVATE)
create_client "$CLIENT_ID1" "$ROOT_URL1" "$VALID_REDIRECT_URIS1" "$VALID_POST_LOGOUT_REDIRECT_URIS1" "$WEB_ORIGINS1" "false"

# Create `greeting` (PUBLIC)
create_client "$CLIENT_ID2" "$ROOT_URL2" "$VALID_REDIRECT_URIS2" "$VALID_POST_LOGOUT_REDIRECT_URIS2" "$WEB_ORIGINS2" "true"

/opt/keycloak/bin/kcadm.sh create users -r "$REALM_NAME" -s username="$USERNAME" -s enabled=true
/opt/keycloak/bin/kcadm.sh set-password -r "$REALM_NAME" --username "$USERNAME" --new-password "$PASSWORD" --temporary=false


echo "Retrieving Client UUID for '$CLIENT_ID2'..."
CLIENT_UUID=$(/opt/keycloak/bin/kcadm.sh get clients -r "$REALM_NAME" --query clientId="$CLIENT_ID2" --fields id --format csv | tail -n 1 | tr -d '"')

echo "Creating role '$ROLE_USER' in client '$CLIENT_ID2'..."
/opt/keycloak/bin/kcadm.sh create clients/"$CLIENT_UUID"/roles -r "$REALM_NAME" -s name="$ROLE_USER"

echo "Creating role '$ROLE_ADMIN' in client '$CLIENT_ID2'..."
/opt/keycloak/bin/kcadm.sh create clients/"$CLIENT_UUID"/roles -r "$REALM_NAME" -s name="$ROLE_ADMIN"

# Assign Both Roles to User
echo "Assigning roles '$ROLE_USER' and '$ROLE_ADMIN' to user '$USERNAME'..."
/opt/keycloak/bin/kcadm.sh add-roles -r "$REALM_NAME" --uusername "$USERNAME" --cclientid "$CLIENT_ID2" --rolename "$ROLE_USER" --rolename "$ROLE_ADMIN"

BFF_CLIENT_UUID=$(/opt/keycloak/bin/kcadm.sh get clients -r "$REALM_NAME" --query clientId="$CLIENT_ID1" --fields id --format csv | tail -n 1 | tr -d '"')
echo $BFF_CLIENT_UUID
# Set custom client secret for bff_cl
echo "Setting custom client secret for '$CLIENT_ID1'..."
/opt/keycloak/bin/kcadm.sh update clients/"$BFF_CLIENT_UUID" -r "$REALM_NAME" -s secret="CLIENT_SECRET"


echo "Setup completed! Both clients are ready."