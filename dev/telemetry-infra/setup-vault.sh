#!/bin/bash

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

# create pki_allocator policy
vault policy write pki_allocator /tmp/pki_allocator.hcl

# For our auth service to authenticate with Vault
vault auth enable approle

# Create the role "auth-service" with pki_allocator policy
vault write auth/approle/role/auth-service policies=pki_allocator

# Read back the role-id
vault read auth/approle/role/auth-service/role-id

# ...and generate a secret_id
vault write -f auth/approle/role/auth-service/secret-id

# For the auth service to issue certificates
vault secrets enable pki
vault write pki/root/generate/internal \
    common_name=telemetry-infra \
    ttl=8760h

vault write pki/roles/telemetry-infra \
    allow_any_name=true \
    max_ttl=72h

echo "

You are now being given a sub-shell with VAULT_ADDR set to $VAULT_ADDR

"
$SHELL
