Nablarch Gradle Plugin
======================

Nablarch開発用のGradleプラグインです。

## 動機

Nablarchの各モジュールは、モジュールの独立性を高めるため、
マルチモジュール構成ではなく、単体のモジュールの集まりとなっています。

マルチモジュール構成であれば、親モジュールに共通設定を記述できるのですが
それができないので、プラグインによって共通化を図ります。

### 代替案
以下のようにapplyでHTTP経由で共通のスクリプトを読み込むこともできます。

```groovy
apply from: 'http://path/to/script.gradle
```

しかし、この方法だと、モジュールのバージョンと共通スクリプトのバージョンを
合わせるのが難しくなる（わかりにくくなる）問題があります。
（プラグインには必ずバージョン番号が付与されるので大丈夫）

## 前準備

以下のようにGradleビルドスクリプトを用意します。

### build.gradle
```groovy
buildscript {
  repositories {
    mavenLocal()
    maven { url "${nablarchRepoReferenceUrl}/internal" }
    jcenter()
  }
  dependencies {
    classpath "com.nablarch.dev:nablarch-gradle-plugin:${nablarchGradlePluginVersion}"
  }
}
```

### gradle.properties

```
nablarchRepoReferenceUrl=http://example.com/artifactory
nablarchGradlePluginVersion=0.0.1-SNAPSHOT
```


## 使用方法

### Nablarch Build プラグイン

このプラグインは、通常のビルドによくある共通設定をします。
* Javaビルド設定（Javaバージョン、文字コード等）
* providedスコープの提供
* テスト実行時のヒープ設定
* カバレッジの有効化
* 参照先リポジトリの設定（Artifactory）
* マニフェストの設定

以下の設定をbuild.gradleに追加します。

```groovy
apply plugin: 'com.nablarch.dev.nablarch-build'
```

以下の設定をgradle.propertiesに追加します。

```
nablarchRepoReferenceUrl=http://example.com/artifactory
```

#### Javaビルド設定

* 文字コード:UTF-8
* Javaバージョン（ソース、ターゲット）:1.6


#### providedスコープの提供

Gradleはデフォルトではprovidedスコープを提供していません。
プラグインを有効にするとprovidedスコープが使用可能になります。

```
dependencies {
  provided 'javax.servlet:servlet-api:2.5'
}
```

IDE(Eclipse, IntelliJ)への連携も同時に行われます。


#### 参照先リポジトリの設定（Artifactory）

以下のリポジトリが設定されます。

* Mavenローカルリポジトリ($HOME/.m2/repository)
* NablarchのMavenリポジトリ
* jcenter

##### SNAPSHOTバージョンの参照

バージョン番号にSNAPSHOTが付与されている場合、参照先リポジトリに以下が追加されます。

``maven { url "${nablarchRepoReferenceUrl}/internal"}``

##### SNAPSHOT以外のバージョンの参照

デフォルトでは、参照先リポジトリに以下が追加されます。

``maven { url "${nablarchRepoReferenceUrl}/staging"}``

プロパティ``nablarchRepoReferenceName``を明示的に設定している場合、
参照先リポジトリには上記の代わりに、以下が追加されます。

``maven { url "${nablarchRepoReferenceUrl}/${nablarchRepoReferenceName}"}``

#### プラグインで設定されるマニフェスト属性

```
| 属性                   | 設定値                                                                             |
| Created-By             | 'Gradle ' + project.gradle.gradleVersion                                           |
| Created-Time           | new Date().format("yyyy-MM-dd HH:mm:ss.SSSZ")                                      |
| Build-Jdk              | System.properties['java.version'] + ' (' + System.properties['java.vendor'] + ')'  |
| Implementation-Title   | project.name                                                                       |
| Implementation-Version | project.version                                                                    |
```

### Nablarch Maven Deploy プラグイン

このプラグインは、通常のデプロイによくある共通設定をします。
* アーティファクトの定義
* デプロイ先URLの指定(Artifactory)
* Gradle上の依存関係で、compileスコープで定義されたものは、pom.xmlにもcompileスコープで出力


以下の設定をbuild.gradleに追加します。

```groovy
apply plugin: 'com.nablarch.dev.nablarch-maven-deploy'
```


以下の設定をgradle.propertiesに追加します。

```
nablarchRepoDeployUrl=http://example.com/artifactory
nablarchRepoUsername=username
nablarchRepoPassword=secret
signing.keyId=A985D5C9
signing.password=password
signing.secretKeyRingFile=/root/.gnupg/secring.gpg
```

ユーザ名、パスワード、鍵情報はgradle.propertiesに記述せずに
gradle実行時のコマンドライン引数で指定するのが望ましいです。

```
gradle clean uploadArchives -PnablarchRepoUsername=username -PnablarchRepoPassword=secret -Psigning.keyId=A985D5C9 -Psigning.password=password -Psigning.secretKeyRingFile=/root/.gnupg/secring.gpg
```



#### SNAPSHOTバージョンのデプロイ

バージョン番号にSNAPSHOTが付与されている場合、デプロイ先は以下のようになります。

``${nablarchRepoDeployUrl}/nablarch-snapshot``

#### SNAPSHOTS以外のデプロイ

デフォルトでは、デプロイ先は以下のようになります。
``${nablarchRepoDeployUrl}/nablarch-staging``


#### プロジェクトプロパティ

プロパティ``nablarchRepoDeployUrl``が設定されていない場合は、
``nablarchRepoReferenceUrl``が代わりにデプロイ先URLとして使用されます。


プロパティ``nablarchRepoName``を明示的に設定している場合、
その指定したリポジトリにデプロイされます。

以下の例では、nablarch-internalにデプロイされます。

```
gradle -PnablarchRepoName=nablarch-internal ^
       -PnablarchRepoUsername=username ^
       -PnablarchRepoPassword=secret ^
       publishMavenPublicationToMavenRepository
```

#### プラグインで設定されるマニフェスト属性

```
| 属性                   | 設定値                                                                             |
| Created-By             | 'Gradle ' + project.gradle.gradleVersion                                           |
| Build-Jdk              | System.properties['java.version'] + ' (' + System.properties['java.vendor'] + ')'  |
| Implementation-Title   | project.name                                                                       |
| Implementation-Version | project.version                                                                    |
```


#### compileスコープの扱い

gradle maven-publishプラグインでは、pom.xml出力時に、compileスコープがruntimeスコープに置き換わります。

```groovy
dependencies {
  compile 'struts:struts:1.2.9'
}
```

の場合、デフォルトでは以下のようなpom.xmlが出力されます。

```xml
<dependency>
  <groupId>struts</groupId>
  <artifactId>struts</artifactId>
  <version>1.2.9</version>
  <scope>runtime</scope>
</dependency>
```


これはこれで合理的な気もするのですが、mavenとの互換性を重視して、
gradle上compileなものはpom.xml上でもcompileとして出力するよう
動作を変更します。

#### 注意点

https://www.gradle.org/docs/current/userguide/tutorial_this_and_that.html#sec:gradle_properties_and_system_properties

```
The properties file in the user's home directory has precedence over property files in the project directories.
ホームディレクトリのgradle.propertiesは、プロジェクトディレクトリのgradle.propertiesの定義を上書きし、優先的に使用されます。
```

### Nablarch SonarQube

このプラグインは、SonarQubeのデプロイによくある共通設定をします。

以下の設定をbuild.gradleに追加します。


```groovy
apply plugin: 'com.nablarch.dev.nablarch-sonarqube'
```

#### プラグインで設定されるsonarプロパティ

プラグインを適用すると、Sonar Runnerプラグインが有効になり、以下の項目が設定されます。

```
| 項目                    | 設定値                                | 備考                                                 |
| sonar.branch            | project.branchName                    | 指定がない場合、空文字                               |
| sonar.sourceEncoding    | project.encoding                      | プロジェクトにencodingが指定されていない場合、エラー |
| sonar.projectName       | project.name                          | -PprojectNameが指定された場合、その値                |
| sonar.projectKey        | ${project.group}:${sonar.projectName} |                                                      |
| sonar.jacoco.reportPath | project.test.jacoco.destinationFile   | jacocoプラグインが有効な場合                         |
```

##### CI時の注意事項

Jenkinsでcheckoutするとディレクトリがworkspaceになり、
project.nameがworkspaceになってしまうので、CI時は、
プロジェクトプロパティ``projectName``で明示的にプロジェクト名を指定します。


#### 実行方法

-PbranchNameでブランチ名を指定します（）。
必要なsonarプロパティは-Dで指定します。


```bash
gradle sonarRunner \
-PprojectName=myproject
-PbranchName=feature/verycool \
-Dsonar.host.url=http://localhost:9000/ \
-Dsonar.jdbc.url=jdbc:h2:tcp://localhost:9092/sonar \
-Dsonar.jdbc.username=sonar \
-Dsonar.jdbc.password=sonar
```

### Nablarch Published API Documentプラグイン

公開APIを作成するタスクを提供します。

#### プロパティ

* apiName : 公開API名称
* publishedFor : 公開対象ロール名

#### 設定例


```groovy
apply plugin: 'com.nablarch.dev.nablarch-published-api'

configurations {
  publishedApiDoc
}

dependencies {
  publishedApiDoc 'com.nablarch.tool:nablarch-toolbox:1.5.0-SNAPSHOT'
  publishedApiDoc 'com.nablarch.framework:nablarch-all:1.5.0-SNAPSHOT'
}

publishedApi {
  apiName = "NablarchTFWApi"
  //roles = ['architect', 'programmer'] // これはデフォルト値なので省略可
}
```

* @Published(tag="architect")が付与されたAPIが出力対象となる
* 公開API一覧ファイルとして``NablarchTFWApiForArchitect.config``が出力される
* @Publishedが付与されたAPIが出力対象となる
* 公開API一覧ファイルとして``NablarchTFWApiForProgrammer.config``が出力される

## プラグイン自身のビルド

### プロパティ

本プラグインのビルドで必須となるプロパティを以下に示す。

| プロパティ名                    | 設定値の内容                                |
|:--------------------------------|:--------------------------------------------|
| signing.keyId                   | GPGキーペアのキーID                         |
| signing.password                | GPG秘密キーのパスフレーズ                   |
| signing.secretKeyRingFile       | GPG秘密キーリングファイル                   |
| nablarchRepoUsername            | リポジトリデプロイユーザ名                  |
| nablarchRepoPassword            | リポジトリデプロイパスワード                |

※GPG秘密キーリングファイルのパス
下記コマンドで確認できる。
```
$ gpg --list-secret-key

/root/.gnupg/secring.gpg
------------------------
...
```

### デプロイコマンド実行例

```
gradlew clean uploadArchives -Psigning.keyId=A985D5C9 -Psigning.password=password -Psigning.secretKeyRingFile=/root/.gnupg/secring.gpg -PnablarchRepoUsername=deployer -PnablarchRepoPassword=password

```
