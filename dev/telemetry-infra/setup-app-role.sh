#!/bin/sh

set -e

cat > /tmp/pki_allocator.hcl <<EOF
path "pki/*" {
  capabilities = ["create", "read", "update", "delete", "list"]
}
EOF

export VAULT_ADDR='http://127.0.0.1:8200'
echo "

FOR DEVELOPMENT MODE
You'll login with the root token: VaultRootToken

"
vault login

echo "
Setting up app-role based authentication...
"

# create pki_allocator policy
vault policy write pki_allocator /tmp/pki_allocator.hcl >> /tmp/setup-app-role.log

# For our auth service to authenticate with Vault
vault auth enable approle >> /tmp/setup-app-role.log

# Create the role "auth-service" with pki_allocator policy
vault write auth/approle/role/auth-service policies=pki_allocator >> /tmp/setup-app-role.log

echo "
Use the following for VAULT_APP_ROLE_ROLE_ID"
# Read back the role-id
vault read auth/approle/role/auth-service/role-id | tee -a /tmp/setup-app-role.log | grep role_id |awk '{print $2}'

echo "
Use the following for VAULT_APP_ROLE_SECRET_ID"
# ...and generate a secret_id
vault write -f auth/approle/role/auth-service/secret-id | tee -a /tmp/setup-app-role.log | grep "secret_id " |awk '{print $2}'

# For the auth service to issue certificates
vault secrets enable pki >> /tmp/setup-app-role.log
vault write pki/root/generate/internal \
    common_name=telemetry-infra \
    ttl=8760h >> /tmp/setup-app-role.log

vault write pki/roles/telemetry-infra \
    allow_any_name=true \
    max_ttl=72h >> /tmp/setup-app-role.log