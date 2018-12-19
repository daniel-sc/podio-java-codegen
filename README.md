podio-java-codegen [![Build Status](https://travis-ci.org/daniel-sc/podio-java-codegen.png)](https://travis-ci.org/daniel-sc/podio-java-codegen)
==================

Tool, that automatically creates Java (wrapper) classes for any given Podio app. Any class includes corresponding from/to Item mappers.
This can easily be used as a maven plugin.

See: https://github.com/daniel-sc/podio-java-codegen/wiki/Getting-started

Additionally there is a simple interface for fetching/updating the wrapper classes. As extras there are proxies for automatic retry on Podio rate limit error/hit and a cache that circumvents unnecessary updates/writes.
