plugins {
    id("org.jetbrains.intellij") version "1.17.3"
    kotlin("jvm") version "1.9.23"
}

group = providers.gradleProperty("pluginGroup").get()
version = providers.gradleProperty("pluginVersion").get()

repositories {
    mavenCentral()
}

intellij {
    version.set(providers.gradleProperty("platformVersion").get())
    type.set(providers.gradleProperty("platformType").get())
    plugins.set(listOf("java"))
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation("com.squareup.okhttp3:okhttp:4.11.0") // 网络请求（股票接口）
    implementation("com.alibaba:fastjson:2.0.32") // JSON解析
}

tasks {
    patchPluginXml {
        sinceBuild.set(providers.gradleProperty("pluginSinceBuild").get())
        untilBuild.set(providers.gradleProperty("pluginUntilBuild").get())
        changeNotes.set(
            """
           <p>Jump Jump 插件为开发者提供便捷的代码跳转功能，提升开发效率。</p>
            <br/>
            <p><b>主要功能：</b></p>
            <ul>
                <li>支持 iBATIS sqlMap 与 Java 方法之间的双向跳转，方便快速定位 SQL 与业务逻辑。</li>
                <li>支持以 biz-context 开头的 Spring 配置文件之间的跳转，以及 biz-context 文件到 Java 类的跳转，简化配置查找。</li>
                <li>新增支持 Facade 接口调用关系的跳转，便于追踪服务调用链路。</li>
            </ul>
            <br/>
            <p><b>注意事项：</b></p>
            <ul>
                <li>本插件目前仍处于实验阶段，功能可能不够稳定。</li>
                <li>建议仅限公司内部少数开发者下载试用，请根据实际需求谨慎安装。</li>
            </ul>
            """.trimIndent()
        )
    }
    compileKotlin {
        kotlinOptions.jvmTarget = "17"
    }
    compileJava {
        targetCompatibility = "17"
        sourceCompatibility = "17"
    }
}

