# How to update Ambassador and Tenant certs for local dev testing

Using the following as an example, you can see if a local dev testing cert is expired:

```
openssl x509 -in out/ambassador.pem -subject -dates -noout
```

If one or more are expired, touch the corresponding CSR JSON input and rebuild the cert files
by using the following:

```
touch ambassador-csr.json
touch tenantA-csr.json
touch tenantB-csr.json

make out/ambassador.pem
make out/tenantA.pem
make out/tenantB.pem
```