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


### 7. 使用 jslint 检测js语法（`<goal>jslint</goal>`）

```xml
<plugin>
    <groupId>com.fh.foundry.maven</groupId>
    <artifactId>minify-maven-plugin</artifactId>
    <version>0.1</version>
    <executions>
        <execution>
            <goals>
		<goal>jslint</goal>
                <goal>compress</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <excludes>
            <exclude>**/*.min.js</exclude>
        </excludes>
    </configuration>
</plugin>
```

### 8. 合并 js/css 文件

好习惯：
- 在网络上，是下载/调用一个大 js 文件而不是几个小文件。
- 在源代码组织中是有源文件。

主要的 js 和 css 框架/lib 提供两个版本的源代码（一个大的，很多小的），或者提供一个在线工具来生成大的。

以下选项允许您在源代码中使用/存储小文件并在构建时生成大文件，合并是在yuicompression之后完成的。

压缩每个 js 和 css 文件并将 `${project.build.directory}/${project.build.finalName}/static/` 下的每个 js 文件合并到 `all.js` 中：

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
        <aggregations> 
            <aggregation> 
              <!-- 合并后删除文件（默认：false） 
              <removeIncluded>true</removeIncluded>
              --> 
              <!-- 在每个连接后插入新行（默认：false）--> 
              <insertNewLine>true</insertNewLine> 
              <output>${project.build.directory}/${project.build.finalName}/static/all.js</output> 
              <!-- 要包含的文件，相对于输出目录的路径或绝对路径--> 
              <!--inputDir>非绝对包含的基本目录，默认为输出的父目录</inputDir --> 
              <includes> 
                <include>${basedir}/src/licenses/license.js</include> 
                <include>**/*.js</include> 
              </includes> 
              <!-- 要排除的文件，相对于输出目录的路径
              <excludes> 
                <exclude>**/*.pack.js</exclude> 
                <exclude>**/compressed.css</exclude> 
              </excludes> 
              --> 
            </aggregation> 
         </aggregations> 
    </configuration>
</plugin>
```

#### 添加标题（版权）

在聚合缩小的 js 文件时，版权标头已被删除，这很好，因为我们不想在输出文件中重复多次。但是，能够在输出文件的开头插入一个会很棒。

对于简单的情况，如果您对所有文件使用相同的标头，则 maven-license-plugin 就足够了，但如果您希望每个聚合具有不同的标头（具有不同许可方案的不同库），这还不够。

- 您将许可证标头放在自己的文件中（例如 license_js.txt）
- 将许可证头聚合到缩小的文件中。

```xml
<includes> 
    <include>${project.build.sourceDirectory}/../webapp/js/mylib/copyright.txt</include> 
    <include>mylib/**/*.js</include> 
  </includes>
```

#### 添加文件名标题

在聚合缩小的 js 或 css 文件时，文件头已被剥离。但是，如果能够在查看聚合文件时轻松识别相应的输入文件，那就太好了。

```xml
<configuration> 
  <aggregations> 
   <aggregation> 
      <!-- 在每个连接后插入新行（默认：false）--> 
      <insertNewLine>真</insertNewLine>
      <!-- 在每个聚合文件前插入文件头（默认：false）--> 
      <insertFileHeader>true</insertFileHeader> 
      <output>${project.build.directory}/${project.build.finalName}/static/ all-3.js</output> 
      <!-- 要包含的文件，相对于输出目录的路径 --> 
      <includes> 
	<include>**/*.js</include> 
      </includes> 
    </aggregation> 
  < /aggregations> 
</configuration> 
```

#### 自动排除先前聚合中包含的通配符匹配

当为同一类型的文件（例如 css）定义了多个聚合时，在使用通配符包含的最终聚合上维护匹配排除可能会很乏味。自动排除已包含在先前聚合中的通配符匹配会很方便。

```xml
<configuration> 
  <aggregations> 
   <aggregation> 
      <output>${project.build.directory}/${project.build.finalName} /static/IE.css</output>
      <!-- 要包含的文件，相对于输出目录的路径 -->
      <includes> 
	<include>**/IE*.css</include> 
      </includes> 
    </aggregation> 
   <aggregation> 
      <!-- 排除之前聚合中包含的任何通配符匹配（默认：false）-- > 
      <autoExcludeWildcards>true</autoExcludeWildcards> 
      <output>${project.build.directory}/${project.build.finalName}/static/everything-except-IE.css</output> 
      <!-- 要包含的文件, 相对于输出目录的路径 --> 
      <includes> 
	<include>**/*.css</include> 
      </includes>
    </aggregation> 
  </aggregations>
</configuration> 
```

### 9. 开启GZip压缩（`<gzip>true</gzip>`）

压缩每个 js 和 css 文件并生成 gzip 版本（gzip 选项不会删除输入文件）：

```xml
<configuration> 
  <gzip>true</gzip> 
</configuration> 
```

### 10. 警告失败

如果出现一些警告，FailOnWarning 允许停止构建过程。

压缩每个 js 和 css 文件并在警告时失败（在 jslint 和/或压缩上）：

```xml
<configuration>
  <failOnWarning>true</failOnWarning>
</configuration>
```

### 11. 结论

在本文中，我们描述了一种将 Javascript 和 CSS 文件的缩小集成到 Maven 工作流程中的好方法。
