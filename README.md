# Digital Systems Distributed Lab

This is a project based on course of [distributed systems](https://www.ds.unipi.gr/en/courses/distributed-systems-2/) at departmen of Digital Systems in University of Piraeus.
The main scope is the creation of a distributed, fault tolerance high throughput logging system.

The core of the system supports a leader election with [Bully Leader Election](https://lass.cs.umass.edu/~shenoy/courses/spring22/lectures/Lec14_notes.pdf) algorithm, also we use [Lamport Logical Clock](https://en.wikipedia.org/wiki/Lamport_timestamp) for distirbuted snapshot and sync over the nodes.

For testing and academic purposes we tried to keep it simple, that's the reason we start multiple Servers in same machine but with different ports.  
