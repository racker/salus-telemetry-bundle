OUT := out

.PHONY: default
default: $(addprefix ${OUT}/, ca-key.pem ca.pem ambassador.pem tenantA.pem tenantB.pem)

${OUT}/ca.pem: ca-csr.json
	mkdir -p ${OUT}
	cfssl gencert -initca -config=cfssl-conf.json ca-csr.json | cfssljson -bare ${OUT}/ca

${OUT}/%.pem: %-csr.json ${OUT}/ca.pem
	mkdir -p ${OUT}
	cfssl gencert -ca ${OUT}/ca.pem -ca-key ${OUT}/ca-key.pem -config=cfssl-conf.json $< | cfssljson -bare ${OUT}/$*
	cat ${OUT}/ca.pem $@ > ${OUT}/$*-chain.pem
	openssl pkcs8 -topk8 -nocrypt -in ${OUT}/$*-key.pem -out ${OUT}/$*-pkcs8-key.pem

