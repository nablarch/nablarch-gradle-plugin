package nablarch.dev

import org.gradle.api.Project
import org.gradle.sonar.runner.plugins.SonarRunnerPlugin
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Rule
import spock.lang.Specification

class NablarchSonarQubePluginSpec extends Specification {

  @Rule
  SystemPropertyRule sysProps = new SystemPropertyRule()

  def "apply() should load the plugin"() {
    given:
    def project = ProjectBuilder.builder().build()

    when:
    project.with {
      apply plugin: 'com.nablarch.dev.nablarch-sonarqube'
      ext {
        encoding = 'UTF-8'
      }
    }

    then:
    project.plugins.hasPlugin(NablarchSonarQubePlugin)
    project.plugins.hasPlugin(SonarRunnerPlugin)
  }

  def "プロジェクトプロパティがsonarのプロパティに反映されること。"() {
    given:
    def project = ProjectBuilder.builder().build()

    when:
    project.with {
      group = 'mygroup'
      ext {
        projectName = 'myproject'
        branchName = 'feature/verycool'
        encoding = 'UTF-8'
      }
      apply plugin: 'com.nablarch.dev.nablarch-sonarqube'
    }

    then:
    def props = getSonarPropsOf(project)
    props['sonar.branch'] == 'feature/verycool'
    props['sonar.sourceEncoding'] == 'UTF-8'
    props['sonar.projectName'] == 'myproject'
    props['sonar.projectKey'] == 'mygroup:myproject'
  }

  def "システムプロパティを設定していると、sonarRunnerの設定に反映されること。"() {
    given:
    def project = ProjectBuilder.builder().build()

    when:
    System.setProperty('sonar.host.url', 'http://example.com/')
    System.setProperty('sonar.jdbc.url', 'jdbc:h2:tcp://example.com:9092/sonar')
    System.setProperty('sonar.jdbc.username', 'username')
    System.setProperty('sonar.jdbc.password', 'secret')

    project.with {
      apply plugin: 'com.nablarch.dev.nablarch-sonarqube'
      ext {
        encoding = 'UTF-8'
      }
    }

    then:
    def props = getSonarPropsOf(project)
    props['sonar.host.url'] == 'http://example.com/'
    props['sonar.jdbc.url'] == 'jdbc:h2:tcp://example.com:9092/sonar'
    props['sonar.jdbc.username'] == 'username'
    props['sonar.jdbc.password'] == 'secret'

  }

  def "jacocoが有効な場合、reportPathが設定されること。"() {
    given:
    def project = ProjectBuilder.builder().build()

    when:
    project.with {
      apply plugin: 'java'
      apply plugin: 'jacoco'
      test {
        jacoco {
          destinationFile = file("${buildDir}/path/to/jacoco/report.exec")
        }
      }
      apply plugin: 'com.nablarch.dev.nablarch-sonarqube'
      ext {
        encoding = 'UTF-8'
      }
    }

    then:
    def props = getSonarPropsOf(project)
    def report = props['sonar.jacoco.reportPath'].replace('\\', '/')
    report =~ '/path/to/jacoco/report.exec$'

  }

  def getSonarPropsOf(Project pj) {
    def sonarRunnerTask = pj.getTasksByName('sonarRunner', false).first()
    return sonarRunnerTask.properties.sonarProperties
  }

}
