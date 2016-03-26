Stub-runner
===========

Runs stubs for service collaborators. Treating stubs as contracts of services allows to use stub-runner as an implementation of 
[Consumer Driven Contracts](http://martinfowler.com/articles/consumerDrivenContracts.html).

### Running stubs

#### Running using main app

You can set the following options to the main class:

```
java -jar stub-runner.jar [options...] 
 -maxp (--maxPort) N            : Maximum port value to be assigned to the
                                  Wiremock instance. Defaults to 15000
                                  (default: 15000)
 -minp (--minPort) N            : Minimal port value to be assigned to the
                                  Wiremock instance. Defaults to 10000
                                  (default: 10000)
 -s (--stubs) VAL               : Comma separated list of Ivy representation of
                                  jars with stubs. Eg. groupid:artifactid1,group
                                  id2:artifactid2:classifier
 -sr (--stubRepositoryRoot) VAL : Location of a Jar containing server where you
                                  keep your stubs (e.g. http://nexus.net/content
                                  /repositories/repository)
 -ss (--stubsSuffix) VAL        : Suffix for the jar containing stubs (e.g.
                                  'stubs' if the stub jar would have a 'stubs'
                                  classifier for stubs: foobar-stubs ).
                                  Defaults to 'stubs' (default: stubs)
 -wo (--workOffline)            : Switch to work offline. Defaults to 'false'
                                  (default: false)

```

### Stub runner configuration

You can configure the stub runner by either passing the full arguments list with the `-Pargs` like this:

```
./gradlew stub-runner-root:stub-runner:run -Pargs="-c pl -minp 10000 -maxp 10005 -s a:b:c,d:e,f:g:h"
```

or each parameter separately with a `-P` prefix and without the hyphen `-` in the name of the param

```
./gradlew stub-runner-root:stub-runner:run -Pc=pl -Pminp=10000 -Pmaxp=10005
```

### Defining collaborators' stubs

You can define global stubs under folder corresponding to groupid/artifactid of your collaborator

```
com/ofg/foo
```

By default stub definitions are stored in `mappings` directory inside stub repository.

#### Stubbing collaborators

For each collaborator defined in project metadata all collaborator mappings (stubs) available in repository are loaded.
Stubs are defined in JSON documents, whose syntax is defined in [WireMock documentation](http://wiremock.org/stubbing.html)

Example:
```json
{
    "request": {
        "method": "GET",
        "url": "/ping"
    },
    "response": {
        "status": 200,
        "body": "pong",
        "headers": {
            "Content-Type": "text/plain"
        }
    }
}
```

Stub definitions are stored in stub repository under the same path as collaborator fully qualified name.
Paths (as long it's inside the directory mentioned above) and names of documents containing stub definitions not play any 
other role than describing stubs' role / purpose.

#### Viewing registered mappings

Every stubbed collaborator exposes list of defined mappings under `__/admin/` endpoint.