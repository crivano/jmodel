Project JModel
====
JLogic is a simple open-source library for converting a Markdown template to Freemarker and HTML. It is specifically designed to be used to build templates for document management systems.

Main goals are:
- The template should be compact
- The template should be easily readable

In a worst case scenario:
- It shoud be possible to replace all Markdown by HTML
- It shoud be possible to replace every template command with plain Freemarker

Example / Usage
====
A basic template like this:

```Markdown
Hi {name}!
```

Will be converted to a Freemarker/HTML code like that:

```FreeMarker
[@interview]
  [@field var='name'/]
[/@interview]

[@document]
  <p>Hi
    [@value var='name'/]!</p>
[/@document]
```

Building
====

JModel is built with Maven. To build from source,

```bash
> mvn package
```

generates a snapshot jar target/jmodel-0.0.1-SNAPSHOT.jar.

To run the test suite locally,

```bash
> mvn test
```

Development
====

JModel is very much in-development, and is in no way, shape, or form guaranteed to be stable or bug-free.  Bugs, suggestions, or pull requests are all very welcome.

License
====
Copyright 2023 Renato Crivano

Licensed under the Apache License, Version 2.0

http://www.apache.org/licenses/LICENSE-2.0
