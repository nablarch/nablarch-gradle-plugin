package nablarch.dev

import org.gradle.api.Project

/**
 * {@link Project}に対するMixin
 *
 * @author T.Kawasaki
 * @since
 */
class ProjectMixin {

  /**
   * プロジェクトから必須のプロパティを取得する。
   * @param project 取得元のプロジェクト
   * @param propertyName 取得するプロパティ名
   * @return プロパティ
   * @throws IllegalStateException 指定した必須プロパティが存在しない場合
   */
  static Object getRequired(Project project, String propertyName)
          throws IllegalStateException {

    if (!project.hasProperty(propertyName)) {
      throw new IllegalStateException(
              "必須のプロジェクトプロパティ[${propertyName}]が設定されていません。" +
              "gradle.propertiesまたはbuild.gradleを見なおして下さい。"
      )
    }
    return project[propertyName]
  }

  /**
   * プロジェクトから任意のプロパティを取得する。
   *
   * @param project 取得元のプロジェクト
   * @param propertyName 取得するプロパティ名
   * @return プロパティ（存在しない場合、null）
   */
  static Object getOptional(Project project, String propertyName) {
    return getOrElse(project, propertyName, null)
  }

  /**
   * プロジェクトからプロパティを取得する。
   * 指定したプロパティが存在しない場合、代替値が返却される。
   *
   * @param project 取得元のプロジェクト
   * @param propertyName 取得するプロパティ名
   * @param alternative 代替値
   * @return プロパティ（存在しない場合、代替値）
   */
  static Object getOrElse(Project project, String propertyName, String alternative) {
    return project.hasProperty(propertyName) ? project[propertyName] : alternative
  }


  /**
   * プロジェクトのバージョン（{@link Project#getVersion()}）が、
   * SNAPSHOTかどうか判定する。
   *
   * @param project 判定対象となるプロジェクト
   * @return SNAPSHOTの場合、真
   */
  static boolean isSnapshotVersion(Project project) {
    String pjVersion = project.version.toString()
    return pjVersion.endsWith('-SNAPSHOT')
  }

  /**
   * （ディレクトリ名ではなく）明示的に設定されたプロジェクト名を取得する。
   * プロジェクトプロパティ'projectName'にプロジェクト名が設定されているものとして
   * 当該プロパティからプロジェクト名を取得する。
   *
   * Jenkinsでcheckoutするとディレクトリがworkspaceになり、
   * project.nameがworkspaceになってしまうので、CI時は、
   * プロジェクトプロパティでプロジェクト名を設定できるようにする。
   *
   * @param project 取得対象プロジェクト
   * @return 明示的に設定されたプロジェクト名
   * @throws IllegalStateException プロジェクトプロパティが設定されていない場合
   */
  static String getExplicitProjectName(Project project) {
    String name = project.getOrElse('projectName', project.name) as String
    if (name.equalsIgnoreCase('workspace')) {
      throw new IllegalStateException(
              "プロジェクトプロパティ'projectName'を設定してください。")
    }
    return name
  }
}
