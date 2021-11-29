Nablarch Gradle Plugin検証用プロジェクト
======================

Gradleプラグインがbuild.gradleに組み込めているかどうかを検証するプロジェクトです。  

## 実行・確認方法

以下のコマンドを実行し、いずれも`BUILD SUCCESSFUL`となれば正常です。
```
./gradlew testNablarchBuildPlugin
```
```
./gradlew testNablarchMavenDeployPlugin
```
```
./gradlew testNablarchSonarQubePlugin
```
```
./gradlew testNablarchPublishedApiPlugin
```
```
./gradlew testNablarchVersionApiPlugin
```
```
./gradlew testJarBaseName
```

## Nablarch Gradle Pluginバージョンアップ時の対応
プラグインを改修した場合は本プロジェクトの修正が必要になることがあります。
必要に応じて下記対応を実施してください。

### acceptance-test/gradle.properties
プラグインバージョン番号をインクリメントさせた場合は下記設定を修正してください。
```
version=0.0.22
```

### acceptance-test/gradle/gradle-wrapper.properties
動作確認用のgradleのバージョンを変更する場合は下記設定を修正してください。
```
distributionUrl=https\://services.gradle.org/distributions/gradle-2.13-bin.zip
```

