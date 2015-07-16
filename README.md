JAX-RSで静的リソースを扱うサンプル
==================================================

## Overview

クラスパス上の `/static` ディレクトリ以下のファイルを静的ファイルとして扱うサンプルです。

テストには [Jersey Test Framework](https://jersey.java.net/documentation/latest/test-framework.html)
を使っていますが `src/main/java` はJAX-RSのAPIにのみ依存するようにしています。

## How to Run

JUnitテストを書いているのでそれを動かしてください。

```
gradle test
```

デフォルトだとインメモリのJerseyコンテナでテストしますがGrizzlyやJettyでもテストできるようにしています。

```
# Grizzly
gradle -Penv=grizzly test
# com.sun.net.httpserver
gradle -Penv=jdk test
# Jetty
gradle -Penv=jetty test
# http://www.simpleframework.org/
gradle -Penv=simple test
```

## License

[Apache License Version 2.0](apache.org/licenses/LICENSE-2.0.txt)

## Author

[@backpaper0](https://twitter.com/backpaper0)

