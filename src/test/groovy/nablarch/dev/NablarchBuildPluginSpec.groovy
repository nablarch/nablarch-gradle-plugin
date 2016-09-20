package nablarch.dev

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class NablarchBuildPluginSpec extends Specification {

  private void applyPluginTo(Project pj) {
    pj.ext {
      nablarchRepoReferenceUrl = 'https://oss.sonatype.org'
    }
    pj.apply plugin: 'com.nablarch.dev.nablarch-build'
  }

  def "apply() should load the plugin"() {
    given:
    Project project = ProjectBuilder.builder().build()

    when:
    applyPluginTo(project)
    project.version = "0.0.1"

    then:

    project.plugins.hasPlugin(NablarchBuildPlugin)
    project.plugins.hasPlugin(JavaPlugin)

    project.sourceCompatibility == JavaVersion.VERSION_1_6

    project.ext.encoding == 'UTF-8'
    project.ext.javaVersion == JavaVersion.VERSION_1_6

    project.configurations.getByName('provided') != null

    project.plugins.hasPlugin('eclipse')

    project.repositories.maven.url as String ==
        'https://oss.sonatype.org/content/groups/staging'
  }



  def "SNAPSHOTバージョンの場合、internalリポジトリが参照先に追加されていること"() {
    given:
    Project project = ProjectBuilder.builder().build()

    when:
    project.version = '1.0.0-SNAPSHOT'
    applyPluginTo(project)

    then:
    project.repositories.maven.url as String ==
        'https://oss.sonatype.org/content/groups/public'
  }
  
  def "参照先リポジトリ名を指定した場合、指定したリポジトリが参照先に追加されていること"() {
    given:
    Project project = ProjectBuilder.builder().build()

    when:
    project.version = '1.0.0'
    project.ext {
      nablarchRepoReferenceName = 'reference-target'
    }
    applyPluginTo(project)

    then:
    project.repositories.maven.url as String ==
        'https://oss.sonatype.org/reference-target'
  }
}
