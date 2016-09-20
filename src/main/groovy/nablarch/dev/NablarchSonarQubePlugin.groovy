package nablarch.dev

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * SonarQubeの設定を行うプラグイン。
 *
 * @author T.Kawasaki
 * @since 1.5.0
 */
class NablarchSonarQubePlugin implements Plugin<Project> {

  /** {@inheritDoc} */
  @Override
  void apply(Project project) {
    Project.mixin(ProjectMixin)
    project.apply(plugin: "sonar-runner")

    // Jenkinsでcheckoutするとディレクトリがworkspaceになり、
    // project.nameがworkspaceになってしまうので、CI時は、
    // -PprojectName=<プロジェクト名>で指定してもらう

    project.sonarRunner {
      sonarProperties {
        def sonarProjectName = project.getExplicitProjectName()
        property "sonar.projectKey", "${project.group}:${sonarProjectName}"
        property "sonar.branch", project.getOrElse('branchName', '')
        property "sonar.sourceEncoding", project.getRequired('encoding')
        property "sonar.projectName", sonarProjectName

        // Jacoco設定が有効な場合
        if (isJacocoEnabled(project)) {
          property "sonar.jacoco.reportPath", project.test.jacoco.destinationFile
        }
      }
    }
  }

  private static boolean isJacocoEnabled(Project pj) {
    return pj.plugins.hasPlugin('java') && pj.plugins.hasPlugin('jacoco')
  }

}


