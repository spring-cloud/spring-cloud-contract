package org.springframework.cloud.contract.verifier.maven.stubrunner;

import java.lang.*;
import java.io.*;
import java.net.*;
import java.util.*;
import groovy.lang.*;
import groovy.util.*;

@javax.inject.Named() @javax.inject.Singleton() @groovy.transform.CompileStatic() @groovy.util.logging.Slf4j() public class AetherStubDownloaderFactory
  extends java.lang.Object  implements
    groovy.lang.GroovyObject {
;
@javax.inject.Inject() public AetherStubDownloaderFactory
(org.eclipse.aether.RepositorySystem repoSystem, org.apache.maven.project.MavenProject project) {}
public  groovy.lang.MetaClass getMetaClass() { return (groovy.lang.MetaClass)null;}
public  void setMetaClass(groovy.lang.MetaClass mc) { }
public  java.lang.Object invokeMethod(java.lang.String method, java.lang.Object arguments) { return null;}
public  java.lang.Object getProperty(java.lang.String property) { return null;}
public  void setProperty(java.lang.String property, java.lang.Object value) { }
public  org.springframework.cloud.contract.stubrunner.AetherStubDownloader build(org.eclipse.aether.RepositorySystemSession repoSession) { return (org.springframework.cloud.contract.stubrunner.AetherStubDownloader)null;}
}
