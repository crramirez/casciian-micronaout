# casciian-micronaut-starter

Micronaut 4.x extension module that exposes Casciian admin sessions through:

- Embedded SSH server (`casciian.ssh.*`)
- Unix domain socket server (`casciian.unix-socket.*`)

Consumer applications provide a `CasciianSessionHandler` bean, and the starter
wires transport servers around it.
