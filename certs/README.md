Drop your corporate/proxy root CA certificate(s) here as `*.crt`
(Base64/PEM-encoded X.509), if your network TLS-intercepts outbound HTTPS
(e.g. a Palo Alto, Zscaler or Netskope proxy). The Dockerfile imports every
`.crt` file in this folder into the JVM's trust store at build time.

Not needed on a network without TLS interception — leave this folder empty.

These files are gitignored (not committed): they're specific to your
company's proxy, not portable across machines, and there's no reason to
share them.
