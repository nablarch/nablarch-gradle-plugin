package nablarch.dev

import org.gradle.api.*
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class NablarchMavenDeployPluginSpec extends Specification {

  private void applyPluginTo(Project pj) {
    pj.ext {
      nablarchRepoDeployUrl = 'https://oss.sonatype.org'
      nablarchRepoUsername='xxxx'
      nablarchRepoPassword='xxxx'
    }

    pj.ext."signing.keyId"='xxxxx'
    pj.ext."signing.password"='xxxxx'
    pj.ext."signing.secretKeyRingFile"='xxxx'

    pj.apply plugin: 'com.nablarch.dev.nablarch-maven-deploy'
  }

  private void applyProjectPropertyNoPlugin(Project pj) {
    pj.ext {
      nablarchRepoDeployUrl = 'https://oss.sonatype.org'
      nablarchRepoReferenceUrl = 'https://oss.sonatype.org/content/groups/staging/artifactory-ref'
      nablarchRepoUsername='xxxx'
      nablarchRepoPassword='xxxx'
    }

    pj.ext."signing.keyId"='xxxxx'
    pj.ext."signing.password"='xxxxx'
    pj.ext."signing.secretKeyRingFile"='xxxx'
  }

  def "apply() should load the plugin"() {
    given:
    Project project = ProjectBuilder.builder().build()

    when:
    applyPluginTo(project)

    then:

    project.plugins.hasPlugin(NablarchMavenDeployPlugin)
    
    

    project.uploadArchives.repositories[0].repository.url as String ==
            'https://oss.sonatype.org/service/local/staging/deploy/maven2'

  }

  def "SNAPSHOTの場合は、snapshotのURLが設定される。"() {
    given:
    Project project = ProjectBuilder.builder().build()

    when:

    project.version = "0.0.1-SNAPSHOT"
    applyPluginTo(project)

    then:

    project.uploadArchives.repositories[0].snapshotRepository.url as String ==
            'https://oss.sonatype.org/content/repositories/snapshots'
  }

  def "ユーザ名とパスワードがプロジェクトのプロパティに設定されていない場合、credentialにも設定されないこと"() {

    given:
    Project project = ProjectBuilder.builder().build()

    when:
    applyProjectPropertyNoPlugin(project)
    project.ext {
        nablarchRepoUsername = null
        nablarchRepoPassword = null
    }
    project.apply plugin: 'com.nablarch.dev.nablarch-maven-deploy'

    then:
    project.uploadArchives.repositories[0].repository.authentication.userName == null
    project.uploadArchives.repositories[0].repository.authentication.password == null
  }

  def "明示的にリポジトリ名が設定された場合、デプロイ先が変更されること。"() {
    given:
    Project project = ProjectBuilder.builder().build()

    when:
    applyProjectPropertyNoPlugin(project)
    project.ext {
      nablarchRepoName = 'nablarch-internal'
    }
    project.apply plugin: 'com.nablarch.dev.nablarch-maven-deploy'

    then:
    project.uploadArchives.repositories[0].repository.url as String ==
            'https://oss.sonatype.org/nablarch-internal'
  }

  def "Manifest属性が設定されていること"() {
    given:
    Project project = ProjectBuilder.builder().withName('myproject').build()

    when:
    applyPluginTo(project)

    then:

    def attributes = project.jar.manifest.attributes
    attributes.containsKey('Created-By')
    attributes.containsKey('Build-Jdk')
    attributes.containsKey('Implementation-Title')
    attributes.containsKey('Implementation-Version')

    attributes['Implementation-Title'] == 'myproject'
    // 他の属性値はテストで設定するのが難しい。
  }

  def "デプロイ先URLが指定されていない場合、参照先URLが使用されること"() {
    given:
    Project project = ProjectBuilder.builder().build()

    when:
    applyProjectPropertyNoPlugin(project)
    project.ext {
      nablarchRepoDeployUrl = null
    }
    project.apply plugin: 'com.nablarch.dev.nablarch-maven-deploy'

    then:
    project.plugins.hasPlugin(NablarchMavenDeployPlugin)

    project.uploadArchives.repositories[0].repository.url as String ==
            'https://oss.sonatype.org/content/groups/staging/artifactory-ref/service/local/staging/deploy/maven2'

  }

  def "プロジェクトプロパティ'projectName'が設定されている場合、Jarのベース名はディレクトリ名でなく当該プロパティ名となること"() {
    given:
    Project project = ProjectBuilder.builder().withName("fugafuga").build()

    when:
    applyProjectPropertyNoPlugin(project)
    project.ext {
      projectName = "hogehoge"
    }
    project.apply plugin: 'com.nablarch.dev.nablarch-maven-deploy'

    then:

    project.jar.baseName == "hogehoge"


  }
}
