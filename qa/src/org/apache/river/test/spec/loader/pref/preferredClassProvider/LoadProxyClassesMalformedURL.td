testClass=LoadProxyClassesMalformedURLException
testCategories=loader,loader_spec
loader.isHttp=false
loader.requireDlPerm=false
loader.annotator=true
loader.codebase="Any_invalid_url"
testjvmargs=-Dnet.jini.loader.ClassLoading.provider=org.apache.river.test.spec.loader.util.QATestPreferredClassProvider
org.apache.river.qa.harness.runkitserver=false 
org.apache.river.qa.harness.runjiniserver=false
