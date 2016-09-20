package nablarch.dev

import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class NablarchPublishedApiPluginSpec extends Specification {

  def "apply() should load the plugin"() {
    given:
    def project = ProjectBuilder.builder().build()

    when:
    project.with {
      repositories {
        maven {
          url 'https://oss.sonatype.org/content/groups/staging'
        }
      }
      ext {
        encoding = 'UTF-8'
      }
      apply plugin: 'com.nablarch.dev.nablarch-published-api'

      publishedApi {
        apiName = 'CoolApi'
      }
    }

    then:
    // プラグインが有効であること
    project.plugins.hasPlugin(NablarchPublishedApiPlugin)

    project.publishedApi.apiName == 'CoolApi'
    project.publishedApi.roles == ['architect', 'programmer'] // default setting

    ['generatePublishedApiDocForArchitect',
     'generatePublishedApiDocForProgrammer'].collect {
      project.getTasksByName(it, false).first()
    }.each { Task t ->
      assert t != null
    }

  }

}
