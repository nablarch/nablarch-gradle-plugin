package nablarch.dev

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.apache.tools.ant.taskdefs.condition.Os

/**
 * Nablarchのビルドを行うためのプラグイン。
 *
 * @author T.Kawasaki
 * @since 1.5.0
 */
class NablarchBuildPlugin implements Plugin<Project> {

  private static final String SNAPSHOT_REPO_NAME = 'content/groups/public'

  private static final String STAGING_REPO_NAME = 'content/groups/staging'

  /** デフォルトのJavaバージョン */
  static final JavaVersion DEFAULT_JAVA_VERSION = JavaVersion.VERSION_1_6

  /** デフォルトのエンコーディング */
  static final String DEFAULT_ENCODING = 'UTF-8'

  /** Mavenリポジトリの参照URL */
  static final String NABLARCH_REPO_REFERENCE_URL = 'nablarchRepoReferenceUrl'

  /** {@inheritDoc} */
  @Override
  void apply(Project project) {
    Project.mixin(ProjectMixin)
    prepareProjectProperties(project)
    prepareJava(project)
    prepareProvidedScope(project)
    prepareTest(project)
    prepareRepository(project)
  }

  /**
   * プロジェクトのプロパティを設定する。
   *
   * @param project 適用対象プロジェクト
   */
  void prepareProjectProperties(Project project) {
    project.ext {
      encoding = DEFAULT_ENCODING
      javaVersion = DEFAULT_JAVA_VERSION
    }
  }

  /**
   * Javaの設定を行う。
   * @param project 適用対象プロジェクト
   */
  void prepareJava(Project project) {
    project.with {
      apply plugin: 'java'
      sourceCompatibility = project.javaVersion
      targetCompatibility = project.javaVersion


      // ソースのエンコーディング設定
      compileJava {
        options.encoding = project.encoding
      }

      // テストソースのエンコーディング設定
      compileTestJava {
        options.encoding = project.encoding
      }

      // gitのハッシュ値を取得
      def revisionHash = ""

      if (Os.isFamily(Os.FAMILY_WINDOWS)) {
          revisionHash = ["cmd", "/c", "cd ${project.projectDir} & git rev-parse HEAD"].execute().in.text.trim()
      } else {
          revisionHash = ["sh", "-c", "cd ${project.projectDir} ; git rev-parse HEAD"].execute().in.text.trim()
      }

      jar.doFirst {
        jar {
            manifest {
                attributes("targetCompatibility": project.targetCompatibility,
                          "git-hash": revisionHash)
            }
        }
      }
    }
  }

  /**
   * gradleはデフォルトでprovidedスコープを持たないので明示的に定義する。
   * @param project
   */
  void prepareProvidedScope(Project project) {
    project.with {
      apply plugin: 'java'
      apply plugin: 'eclipse-wtp'
      apply plugin: 'idea'


      configurations {
        provided
      }

      // compileスコープにprovidedを追加
      sourceSets {
        main {
          compileClasspath += configurations.provided
        }
        test {
          compileClasspath += configurations.provided
          runtimeClasspath += configurations.provided
        }
      }

      // IDEにprovidedの依存関係を追加する。
      eclipse {
        classpath.plusConfigurations += [configurations.provided]
      }
      idea {
        module {
          scopes.PROVIDED.plus += [configurations.provided]
        }
      }
    }
  }

  /**
   * テスト関連の設定を行う。
   * @param project 適用対象プロジェクト
   */
  void prepareTest(Project project) {

    project.with {
      /** テスト設定 */
      test {
        maxHeapSize = '1024m'
        minHeapSize = '512m'

        // デフォルトエンコーディングを強制的に変更(HereIs対応)
        jvmArgs '-Dfile.encoding=utf-8',
                '-da',
                '-XX:-UseSplitVerifier',
                '-Duser.language=ja',
                '-Duser.region=JP'
      }
    }
    
    project.with {
      dependencies {
        testRuntime 'com.h2database:h2:1.4.191'
        testCompile 'com.nablarch.dev:nablarch-test-support:+'
      }
    }
  }

  /**
   * 参照リポジトリの設定を行なう。
   * @param project 適用対象プロジェクト
   */
  void prepareRepository(Project project) {
    project.with {
      repositories {
        mavenLocal()
        maven { url resolveRepoUrl(project)}
        jcenter()
      }
    }
  }

  /**
   * 参照リポジトリのURLを取得する。
   * @param project 適用対象プロジェクト
   * @return 参照リポジトリのURL
   */
  private static String resolveRepoUrl(Project project) {
    def repoName = resolveRepoName(project)
    def baseUrl = project.getRequired(NABLARCH_REPO_REFERENCE_URL)
    return "${baseUrl}/${repoName}"
  }

  /**
   * 参照リポジトリ名を取得する。
   * モジュールのバージョンがSNAPSHOTかどうかでリポジトリ名を切り替える。
   * @param project 適用対象プロジェクト
   * @return 参照リポジトリ名
   */
  private static String resolveRepoName(Project project) {
    if (project.snapshotVersion) {
      return SNAPSHOT_REPO_NAME
    }
    return project.getOrElse('nablarchRepoReferenceName', STAGING_REPO_NAME)
  }
}
