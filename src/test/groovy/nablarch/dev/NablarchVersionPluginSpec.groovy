package nablarch.dev

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class NablarchVersionPluginSpec extends Specification {

    def setupSpec() {
      def propPath=System.properties.'user.home' + '/.gradle/gradle.properties'
      def propFile = new File(propPath)

      if(propFile.exists()) {
        def Properties p = new Properties()
        p.load(propFile.newDataInputStream())

        System.properties['http.proxyHost'] = p.'systemProp.http.proxyHost' ?: ""
        System.properties['http.proxyPort'] = p.'systemProp.http.proxyPort' ?: ""
        System.properties['https.proxyHost'] = p.'systemProp.https.proxyHost' ?: ""
        System.properties['https.proxyPort'] = p.'systemProp.https.proxyPort' ?: ""
        System.properties['http.nonProxyHosts'] = p.'systemProp.http.nonProxyHosts' ?: ""
        System.properties['https.nonProxyHosts'] = p.'systemProp.https.nonProxyHosts' ?: ""
      }
    }

  def project = ProjectBuilder.builder().build()

  def "プラグインが正しくロードできること"() {
    when:
    project.with {
      apply plugin: 'com.nablarch.dev.nablarch-version'
    }

    then:
    project.plugins.hasPlugin(NablarchVersionPlugin)
  }

  def "ブランチ名に対応したプロパティファイルを読み込めること"() {
    setup:
    def process = GroovyMock(ProcessImpl, global: true)
    process.text >> "master\r\n"


    when:
    project.with {
      apply plugin: 'com.nablarch.dev.nablarch-version'
    }

    then:
    assert project.hasProperty("nablarchCoreVersion")
  }

  def "ブランチ名に対応したプロパティファイルが存在しない場合にdevelop.propertiesが読み込めること"() {
    setup:
    def process = GroovyMock(ProcessImpl, global: true)
    process.text >> "develop\r\n"

    when:
    project.with {
      apply plugin: 'com.nablarch.dev.nablarch-version'
    }

    then:
    assert project.hasProperty("nablarchCoreVersion")
  }

  def "プロパティファイルに設定された値が読み込めること"() {
    setup:
    def process = GroovyMock(ProcessImpl, global: true)
    process.text >> "master\r\n"
    def url = GroovySpy(URL, global: true,
            constructorArgs: ["https://github.com/nablarch/nablarch-module-version/blob/master/master.properties?raw=true"])
    // テスト用のプロパティファイルを読み込む
    url.openStream() >> Thread.currentThread().contextClassLoader.getResourceAsStream("master.properties")

    when:
    project.with {
      apply plugin: 'com.nablarch.dev.nablarch-version'
    }

    then:
    assert project.testVersion == "1.0.0"
  }
}
