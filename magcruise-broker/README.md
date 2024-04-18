MAGCruiseBroker
===============
MAGCruiseCoreのプロセスを管理し，MAGCruiseCoreをMAGCruiseWebGUIと繋ぐシステムです．

## ビルド方法
### Eclipseビルド環境の構築

 2. Apache Tomcat 7が無ければインストールし，Eclipseでサーバ設定を追加します．
 3. JDKは7です．
 4. Workspaceの文字コードはUTF-8にします．
 

## MAGCruiseBrokerのデプロイ
### はじめに
この文書では，以下を想定して説明します．

* 配備ディレクトリ: /srv/tomcat/webapps_magcruise/magcruise
* デプロイURL(index.htmlによる利用): http://localhost/magcruise/
* デプロイURL(MAGCruiseWebUIによる利用): http://localhost/magcruise-it/

```
/srv/tomcat/webapps_magcruise/magcruise
    META-INF/
    ui_files/
    WEB-INF/
    index.html
```

#### 動作環境
openSUSE13.1上で動作を確認しています．以下は動作環境の一例であり，必要条件ではありません．
* Tomcat Apache Tomcat/8.0.33
* Java 1.8.0
* Apache 2.2.22 (actions alias auth_basic authz_host authn_file authz_groupfile  authz_user autoindex cgi dir env expires include log_config mime negotiation setenvif ssl userdir reqtimeout authn_core authz_core proxy proxy_http proxy_ajp rewrite headers ws_tunnel)


### 手順 (index.htmlによる利用)
* MAGCruieBrokerをcloneし，Eclipseプロジェクトをインポートします．また，magcruise/WEB-INF/以下のサブモジュールも取得し，インポートします．
```
git clone git@github.com:MAGCruise/MAGCruiseBroker.git
cd MAGCruiseBroker
git submodule init
git submodule update
```

* Tomcatのwebappsとは別の場所に，webapps_magcruiseをコピーします．またwebapps_magcruise/magcruise/WEB-INFに，Tomcat実行ユーザの書き込み権限を与えます．
```bash
drwxrwxr-x 2 root tomcat 4096  4月 01 12:20 /srv/tomcat/webapps_magcruise
```

* コンテキスト設定ファイル(/etc/tomcat/conf/Catalina/localhost/magcruise.xml)を設置します．サンプルがwebapps_magcruise/etc/以下にあります．
```xml
<Context path="/magcruise" docBase="/srv/tomcat/webapps_magcruise/magcruise" />
```

* Tomcatを再起動するとmagcruiseが配備されます．http://localhost:8080/magcruise/にアクセスすると，開発画面が表示されます．

apache経由でアクセスしたい場合は，/etc/apache2/conf.d/mod_proxy_ajp.conf を編集して設定追加し，apacheを再起動します．

```
ProxyPass /magcruise ajp://localhost:8009/magcruise
ProxyPass /magcruise-it ajp://localhost:8009/magcruise-it
```

### 手順 (MAGCruiseWebUIによる利用)
ここまででTomcat上でMAGCruiseCoreが動作することが確認できました．次に，WebUIと連携させるためのWebアプリケーションをTomcat上に配備します．

* コンテキスト設定ファイル(/etc/tomcat/conf/Catalina/localhost/magcruise-it.xml)を設置します．webUIUrlには，MAGCruiseWebUIの設置URLにjsonを加えたパスを指定して下さい．サンプルがwebapps_magcruise/etc/以下にあります．

```xml
<Context path="/magcruise-it" docBase="/srv/tomcat/webapps_magcruise/magcruise">
    <Parameter name="webUIUrl" value="http://localhost/world/json" />
</Context>
```

* MAGCruiseWebUI側の設定は，https://github.com/magcruise/MAGCruiseWebUI/ を参照下さい．

## 関連情報
### MAGCruiseCoreController設定ファイル(MAGCruiseCoreController.xml)
MAGCruiseCoreController.xmlの設置場所はwebアプリケーションのWEB-INF/servicesです．

#### magcruiseが使用するJava
```xml
<property name="javaHome"
  value="/Library/Java/Java.../jdk1.7.0.jdk/Contents/Home"
  />
```

#### MAGCruiseCoreのベースディレクトリ
```xml
<property name="magqHome" value="WEB-INF/MAGQ" />
```

#### ゲームスクリプトの直前および直後に実行するスクリプト
* /またはC:等ドライブレターから始まる場合は絶対パスとして認識する．それ以外はWebアプリケーションのディレクトリからの相対パス．
```xml
<property name="headerScript"
 value="(load (string-append
   *script-base*
   &quot;/framework/magcruise-framework.scm&quot;))"
 />
<property name="footerScript"
 value="(load (string-append
   *script-base*
   &quot;/framework/magcruise-execution.scm&quot;))"
 />
```
#### magcruiseからのメッセージの送信先URL
* magcruise.xmlなどコンテキストを設定するtomcatの設定ファイルよりも優先される．

```xml
<property name="webUIUrl"
  value="http://localhost/world/json" />
```
#### magcruise起動時にkawaに渡すオプション
```xml
<property name="kawaOpts">
  <list>
    <value>&#45;-warn-undefined-variable=no</value>
    <value>&#45;-warn-invoke-unknown-method=no</value>
    <value>&#45;-debug-error-prints-stack-trace</value>
  </list>
</property>
```

### コンテキスト設定ファイル (magcruise.xml, magcruise-it.xml)
* **magcruise-it.xml** の設置場所はTomcatのconf/Catalina/localhostです．
```xml
<Parameter name="webUIUrl"
  value="http://localhost/world/json" />
```


### Cross-domainでのMAGCruise利用について
WEB-INF/web.xmlのJsonRpcServletのパラメータを追加することで，Cross-domainで利用できるようになります．以下に該当部分を示します．

```xml
  <servlet>
    <servlet-name>JsonRpcServlet</servlet-name>
    <servlet-class>jp.go.nict.langrid.servicecontainer.handler.jsonrpc.servlet.JsonRpcServlet</servlet-class>
    <init-param>
        <param-name>additionalResponseHeaders</param-name>
        <param-value>Access-Control-Allow-Origin: *</param-value>
    </init-param>
  </servlet>
```
additionalResponseHeadersには，追加するレスポンスヘッダをurl-encodeしたものをカンマ(,)区切りで指定できます．
