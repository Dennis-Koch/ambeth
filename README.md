<!---
![freeCodeCamp Social Banner](https://s3.amazonaws.com/freecodecamp/wide-social-banner.png)
[![Throughput Graph](https://graphs.waffle.io/freecodecamp/freecodecamp/throughput.svg)](https://waffle.io/freecodecamp/freecodecamp/metrics)
[![Join the chat at https://gitter.im/freecodecamp/freecodecamp](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/freecodecamp/freecodecamp?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
[![Known Vulnerabilities](https://snyk.io/test/github/freecodecamp/freecodecamp/badge.svg)](https://snyk.io/test/github/freecodecamp/freecodecamp)
[![Build Status](https://travis-ci.org/freeCodeCamp/freeCodeCamp.svg?branch=staging)](https://travis-ci.org/freeCodeCamp/freeCodeCamp)
[![Pull Requests Welcome](https://img.shields.io/badge/PRs-welcome-brightgreen.svg?style=flat)](http://makeapullrequest.com)
[![first-timers-only Friendly](https://img.shields.io/badge/first--timers--only-friendly-blue.svg)](http://www.firsttimersonly.com/)
--->


Welcome to Ambeth, the next generation distributed object-relational mapping framework!
=======================

Key Features
* introduces a powerful concept of a federated information model (FIM) to solve the limitations of the "persisted-entity" concept like JPA and others work with
* supports OSGi (allows also to distribute the FIM across several OSGi bundles weave at load time, allows also several different FIM versions at the same OSGi runtime)
* allows to integrate any arbitrary data repository as entities in this FIM (SQL is just one kind, others: file systems, NoSQL databases, REST/SOAP webservices, news feeds, ...)
* supports data binding out-of-the-box. this is also true across the optional remoting layer!
* integrates a distributed entity cache, connected to an integrated event bus. so the cache ALWAYS provides up-to-date information transparently
* supports lazy loading of relations, batch operations, pre-fetching queries and all CRUD operations out of the box (no need to develop a DAO layer or stubs)
* supports all of the above also via a remoting layer (so machine-to-machine communications or rich-clients can directly work with the federated information model (FIM)
* optionally provides also data transfer object mapping, but in the default behavior no mapping overhead is necessary on any layer (the FIM is completely available in the client, the server and gets directly persisted to an arbitrary data repository)
* very fast integrated SQL entity mapping which uses consistently SQL batch operations
* supports Oracle Database, PostgreSQL as default adapters for SQL connectivity
* in progress: H2, HSQLDB, MariaDB, MS SQLServer

Ambeth allows developers to build applications fast, with no compromise in runtime performance, entity model flexibility, modularity and scalability.

## 3.0.1 Release

Ambeth `3.0.1` is now available (April 2017).


### [Join our community here](TODO).

Found a bug?
------------

Do not file an issue until you have followed these steps:

1. Read the [Help I've Found a Bug](http://todo) article and follow its instructions.
2. Ask for confirmation in the appropriate [Help Room](http://todo).
<!---
3. Please *do not* open an issue without a 3rd party confirmation of your problem.
--->

Contributing
------------

We welcome pull requests any developer! Please follow [these steps](CONTRIBUTING.md) to contribute.

License
-------

Copyright (c) 2017 Dennis Koch.

The content of this repository is bound by the [Apache License](./LICENSE.md).