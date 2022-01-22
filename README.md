# minify-maven-plugin
### 在Maven构建阶段进行js和css文件压缩（使用YUICompressor）
### 此工程来自 https://davidb.github.io/yuicompressor-maven-plugin/ 并进行了部分修改。

### 以下内容翻译自：https://www.baeldung.com/maven-minification-of-js-and-css-assets

### 1.概述

本文展示了如何将 Javascript 和 CSS 资源压缩为构建步骤，并使用 Spring MVC 提供生成的文件。

我们将使用YUI Compressor作为底层缩小库和YUI Compressor Maven 插件将其集成到我们的构建过程中。

### 2.Maven插件配置

首先，我们需要在 pom.xml 文件中声明我们将使用压缩插件并执行压缩目标。

这将压缩 `src/main/webapp` 下的所有 `.js` 和 `.css` 文件，因此 foo.js 将被缩小为 foo-min.js 并且 myCss.css 将被缩小为 myCss-min.css：

```xml
<plugin>
    <groupId>com.fh.foundry.maven</groupId>
	  <artifactId>minify-maven-plugin</artifactId>
    <version>0.1</version>
    <executions>
        <execution>
            <goals>
                <goal>compress</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

我们的 `src/main/webapp` 目录包含以下文件：

```
js/
├── foo.js
├── jquery-1.11.1.min.js
resources/
└── myCss.css
```

执行 `mvn clean package` 后，生成的 WAR 文件将包含以下文件：

```
js/
├── foo.js
├── foo-min.js
├── jquery-1.11.1.min.js
├── jquery-1.11.1.min-min.js
resources/
├── myCss.css
└── myCss-min.css
```

### 3. 保持文件名相同

在这个阶段，当我们执行 `mvn clean package` 时，插件会创建 foo-min.js 和 myCss-min.css。由于我们最初在引用文件时使用了 foo.js 和 myCss.css，因此我们的页面仍将使用原始的非缩小文件，因为缩小文件的名称与原始文件不同。

为了防止同时拥有 foo.js / foo-min.js 和 myCss.css / myCss-min.css 并在不更改名称的情况下缩小文件，我们需要使用 `nosuffix` 选项配置插件，如下所示：

```xml
<plugin>
    <groupId>com.fh.foundry.maven</groupId>
	  <artifactId>minify-maven-plugin</artifactId>
    <version>0.1</version>
    <executions>
        <execution>
            <goals>
                <goal>compress</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <nosuffix>true</nosuffix>
    </configuration>
</plugin>
```

现在当我们执行 `mvn clean package` 时，我们将在生成的 WAR 文件中有以下文件：

```
js/
├── foo.js
├── jquery-1.11.1.min.js
resources/
└── myCss.css
```

### 4.WAR插件配置

保持文件名相同有副作用。它会导致 WAR 插件用原始文件覆盖缩小的 foo.js 和 myCss.css 文件，因此我们在最终输出中没有文件的缩小版本。foo.js文件在缩小之前包含以下行：

```js
function testing() {
    alert("Testing");
}
```

当我们检查生成的 WAR 文件中foo.js文件的内容时，我们看到它具有原始内容而不是缩小后的内容。为了解决这个问题，我们需要为压缩插件指定一个 `webappDirectory` 并从 WAR 插件配置中引用它。

```xml
<plugin>
    <groupId>com.fh.foundry.maven</groupId>
	  <artifactId>minify-maven-plugin</artifactId>
    <version>0.1</version>
    <executions>
        <execution>
            <goals>
                <goal>compress</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <nosuffix>true</nosuffix>
        <webappDirectory>${project.build.directory}/min</webappDirectory>
    </configuration>
</plugin>

<plugin>
  <artifactId>maven-war-plugin</artifactId>
  <configuration>
    <webResources>
        <resource>
            <directory>${project.build.directory}/min</directory>
        </resource>
    </webResources>
  </configuration>
</plugin>
```

在这里，我们将 min 目录指定为缩小文件的输出目录，并配置 WAR 插件以将其包含在最终输出中。

现在我们在生成的 WAR 文件中有了缩小的文件，它们的原始文件名为 foo.js 和 myCss.css。我们可以检查foo.js看看它现在有以下缩小的内容：

```js
function testing(){alert("Testing")};
```

### 5.排除已经缩小的文件

第三方 Javascript 和 CSS 库可能有可供下载的缩小版本。如果您碰巧在项目中使用其中之一，则无需再次处理它们。

在构建项目时，包括已经缩小的文件会产生警告消息。例如，jquery-1.11.1.min.js是一个已经缩小的 Javascript 文件，它会在构建过程中导致类似于以下的警告消息：

```
[WARNING] .../src/main/webapp/js/jquery-1.11.1.min.js [-1:-1]: 
Using 'eval' is not recommended. Moreover, using 'eval' reduces the level of compression!
execScript||function(b){a. ---> eval <--- .call(a,b);})
[WARNING] ...jquery-1.11.1.min.js:line -1:column -1: 
Using 'eval' is not recommended. Moreover, using 'eval' reduces the level of compression!
execScript||function(b){a. ---> eval <--- .call(a,b);})
```

要从进程中排除已经缩小的文件，请使用排除选项配置压缩器插件，如下所示：

```xml
<plugin>
    <groupId>com.fh.foundry.maven</groupId>
	  <artifactId>minify-maven-plugin</artifactId>
    <version>0.1</version>
    <executions>
        <execution>
            <goals>
                <goal>compress</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <nosuffix>true</nosuffix>
        <webappDirectory>${project.build.directory}/min</webappDirectory>
        <excludes>
            <exclude>**/*.min.js</exclude>
        </excludes>
    </configuration>
</plugin>
```

这将排除文件名以min.js结尾的所有目录下的所有文件。执行 mvn clean package 现在不会产生警告消息，并且构建不会尝试缩小已经缩小的文件。


### 6. 结论

在本文中，我们描述了一种将 Javascript 和 CSS 文件的缩小集成到 Maven 工作流程中的好方法。
