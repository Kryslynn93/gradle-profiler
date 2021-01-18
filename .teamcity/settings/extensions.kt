import jetbrains.buildServer.configs.kotlin.v2019_2.BuildType
import jetbrains.buildServer.configs.kotlin.v2019_2.CheckoutMode
import jetbrains.buildServer.configs.kotlin.v2019_2.DslContext
import jetbrains.buildServer.configs.kotlin.v2019_2.ParametrizedWithType

fun BuildType.agentRequirement(os: Os) {
    requirements {
        contains("teamcity.agent.jvm.os.name", os.requirementName)
    }
}

fun toolchainConfiguration(os: Os) = listOf("-Porg.gradle.java.installations.auto-detect=false",
    "-Porg.gradle.java.installations.auto-download=false",
    """"-Porg.gradle.java.installations.paths=%${os.name}.java8.oracle.64bit%,%${os.name}.java11.openjdk.64bit%"""").joinToString(" ")

fun ParametrizedWithType.javaHome(os: Os, javaVersion: JavaVersion) {
    param("env.JAVA_HOME", javaVersion.javaHome(os))
}

enum class JavaVersion(private val javaHomePostfix: String) {
    ORACLE_JAVA_8("java8.oracle.64bit"),
    OPENJDK_11("java11.openjdk.64bit");

    fun javaHome(os: Os) = "%${os.name}.$javaHomePostfix%"
}

fun BuildType.gradleProfilerVcs() {
    vcs {
        root(DslContext.settingsRoot)
        checkoutMode = CheckoutMode.ON_AGENT
    }
}
