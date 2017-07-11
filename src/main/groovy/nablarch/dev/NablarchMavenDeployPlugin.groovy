package nablarch.dev

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.XmlProvider
import org.gradle.api.artifacts.Dependency
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.artifacts.maven.MavenDeployment 

/**
 * Mavenリポジトリ(Artifactory)にアーティファクトをデプロイするためのプラグイン。
 * 以下のプロジェクトプロパティが必要。
 * <ul>
 *   <li>nablarchRepoDeployUrl</li>
 *   <li>nablarchRepoUsername</li>
 *   <li>nablarchRepoPassword</li>
 * </ul>
 *
 * @author T.Kawasaki
 * @since 1.5.0
 */
class NablarchMavenDeployPlugin implements Plugin<Project> {

  private static final String SNAPSHOT_REPO_NAME = "content/repositories/snapshots"

  private static final String RC_REPO_NAME = 'service/local/staging/deploy/maven2'

  /** {@inheritDoc} */
  @Override
  void apply(Project project) {
    Project.mixin(ProjectMixin)
    project.apply plugin: 'java'
    defineArtifact(project)
    prepareJarName(project)
    prepareManifest(project)
    prepareJavadoc(project)
    preparePublishing(project)
  }

  /**
   * アーティファクトの定義を行う。
   *
   * @param project
   */
  void defineArtifact(Project project) {
    project.with {

      // *.source.jarを作成する
      task([type: Jar, dependsOn: 'classes'], 'sourcesJar') {
        classifier = 'sources'
        from sourceSets.main.allJava
      }

      // *.doc.jarを作成する
      task([type: Jar, dependsOn: 'classes'], 'javadocJar') {
        classifier = 'javadoc'
        from javadoc
      }
      
      // javaDoc生成時の文字コードを指定
      javadoc {
        options.encoding = 'UTF-8'
      }

      // Artifact定義
      artifacts {
        archives sourcesJar, javadocJar
      }
    }
  }

  /**
   * プロジェクトの成果物Jarファイルのベース名を設定する。
   *
   * @param project プロジェクト
   */
  void prepareJarName(Project project) {
    project.jar.baseName = project.getExplicitProjectName()
  }

  /**
   * Manifestファイルの設定を行なう。
   * @param project 適用対象プロジェクト
   */
  void prepareManifest(Project project) {

    project.with {
      jar {
        manifest {
          attributes(
                  'Created-By': 'Gradle ' + project.gradle.gradleVersion,
                  'Build-Jdk' : System.properties['java.version'] + ' (' + System.properties['java.vendor'] + ')',
                  'Implementation-Title': project.name,
                  'Implementation-Version': project.version,
                  )
        }
      }
    }
  }

  /**
   * Javadocの設定を行う。
   * @param project 適用対象プロジェクト
   */
  void prepareJavadoc(Project project) {
       project.with {
           javadoc {
               if (JavaVersion.current().isJava8Compatible()) {
                   options.addStringOption('Xdoclint:none', '-quiet')
               }
           }
       }
  }

  /**
   * アーティファクト公開の設定を行う。
   *
   * @param project
   */
  void preparePublishing(Project project) {

      project.with {

          apply plugin: 'maven'
          apply plugin: 'signing'

          // providedスコープを利用している場合、providedスコープのライブラリをクラスパスに
          // 追加しておかないとjavadoc生成時にエラーとなるため対応
          if(project.configurations.hasProperty('provided')) {
              javadoc.classpath += project.configurations.provided
          }

          signing {
              sign configurations.archives
          }

          signArchives.onlyIf { project.hasProperty('signing.keyId') }

          uploadArchives {
              repositories {
                  mavenDeployer {
                      beforeDeployment {
                          MavenDeployment deployment ->
                            if (project.hasProperty('signing.keyId')) {
                              signing.signPom(deployment)
                            }
                      }

                      repository(url: resolveRepoUrl(project)) {
                          authentication(userName: project.getOptional('nablarchRepoUsername'), password: project.getOptional('nablarchRepoPassword'))
                          if (System.properties['https.proxyHost']) {
                              proxy(host: System.properties['https.proxyHost'], port: System.properties['https.proxyPort'].toInteger(), type: "https")
                          }
                      }
    
                      snapshotRepository(url: resolveSnapshotRepoUrl(project)) {
                          authentication(userName: project.getOptional('nablarchRepoUsername'), password: project.getOptional('nablarchRepoPassword'))
                          if (System.properties['https.proxyHost']) {
                              proxy(host: System.properties['https.proxyHost'], port: System.properties['https.proxyPort'].toInteger(), type: "https")
                          }
                      }
    
                      pom.project {
                          name project.name 
                          packaging 'jar'
                          description 'Nablarch Framework.'
                          url 'https://github.com/nablarch'
    
                          scm {
                              connection "scm:git:git://github.com/nablarch/${project.name}.git"
                              developerConnection "scm:git:ssh://github.com/nablarch/${project.name}.git"
                              url "https://github.com/nablarch/${project.name}/tree/master"
                          }
    
                          licenses {
                              license {
                                  name 'The Apache License, Version 2.0'
                                      url 'http://www.apache.org/licenses/LICENSE-2.0.txt'
                              }
                          }
    
                          developers {
                              developer {
                                  id 'nablarch'
                                  name 'Nablarch'
                                  email 'nablarch@tis.co.jp'
                                  organization='Nablarch'
                                  organizationUrl 'https://github.com/nablarch'
                              }
                          }
                      }
                  }
              }
          }
      }
  }

  private static Closure<Boolean> isRuntimeScoped(Project project) {
    return { Node node ->
      node.scope.text() == 'runtime' && project.configurations.compile.allDependencies.find { Dependency dep ->
        dep.group == node.groupId.text() && dep.name == node.artifactId.text()
      }
    }
  }

  /**
   * アップロード先のMavenリポジトリのURLを取得する。
   *
   * @param project プロジェクト
   * @return デプロイ先のURL
   */
  private static String resolveRepoUrl(Project project) {
    def repoName = resolveRepoName(project)
    def baseUrl = getBaseUrl(project)
    return "${baseUrl}/${repoName}"
  }

  /**
   * アップロード先のMavenスナップショットリポジトリのURLを取得する。
   *
   * @param project プロジェクト
   * @return デプロイ先のURL
   */
  private static String resolveSnapshotRepoUrl(Project project) {
    def baseUrl = getBaseUrl(project)
    return "${baseUrl}/" + SNAPSHOT_REPO_NAME
  }

  /**
   * アップロード先のMavenリポジトリのベースURLを取得する。
   *
   * @param project プロジェクト
   * @return リポジトリのベースURL
   */
  private static String getBaseUrl(Project project) {
    return project.getOptional('nablarchRepoDeployUrl') ?:
            project.getRequired(NablarchBuildPlugin.NABLARCH_REPO_REFERENCE_URL)
  }

  /**
   * アップロード先のローカルリポジトリ名を取得する。
   *
   * @param project
   * @return
   */
  private static String resolveRepoName(Project project) {
    return project.getOrElse('nablarchRepoName', RC_REPO_NAME)
  }
}
