package gvm

import java.nio.file.FileSystems
import java.nio.file.Files
import java.nio.file.Path

import static cucumber.api.groovy.EN.And

And(~'^the candidate "([^"]*)" version "([^"]*)" is installed$') { String candidate, String version ->
	def file = "${gvmDir}/${candidate}/${version}" as File
	if (!file.exists()) println bash.output
	assert file.exists()
}

And(~'^the candidate "([^"]*)" version "([^"]*)" is not installed$') { String candidate, String version ->
	def directory = FileSystems.default.getPath("$gvmDir/$candidate/$version")
	if (Files.exists(directory)) println bash.output
	assert ! Files.exists(directory)
}

And(~'^the candidate "([^"]*)" version "([^"]*)" is already installed and default$') { String candidate, String version ->
    def candidateVersion = prepareCandidateFolder("$gvmDir", candidate, version)
    def currentLink = FileSystems.default.getPath("$gvmDir/$candidate/current")
    Files.createSymbolicLink currentLink, candidateVersion
}

And(~'^the candidate "([^"]*)" version "([^"]*)" is the default$') { String candidate, String version ->
    def localVersion = FileSystems.default.getPath("$gvmDir/$candidate/$version")
    def currentLink = FileSystems.default.getPath("$gvmDir/$candidate/current")
    Files.createSymbolicLink currentLink, localVersion
}

And(~'^the candidate "([^"]*)" version "([^"]*)" is already installed but not default$') { String candidate, String version ->
    prepareCandidateFolder "$gvmDir", candidate, version
}

And(~'^I do not have a "([^"]*)" candidate installed$') { String candidate ->
    def candidateDir = FileSystems.default.getPath("${gvmDir}/${candidate}")
    assert ! candidateDir.toFile().listFiles()
}

And(~'^the candidate "([^"]*)" does not exist$') { String candidate ->
    def candidateDir = "${gvmDir}/${candidate}" as File
    candidateDir.deleteDir()
    assert ! candidateDir.exists()
}

And(~'^I have a local candidate "([^"]*)" version "([^"]*)" at "([^"]*)"$') { String candidate, String version, String directory ->
    prepareLocalCandidateFolder directory, candidate, version
}

And(~'^the candidate "([^"]*)" version "([^"]*)" is linked to "([^"]*)"$') { String candidate, String version, String directory ->
    def fileSystem = FileSystems.default

    def versionLocation = "$gvmDir/$candidate/$version"
    def versionFolder = fileSystem.getPath(versionLocation)

    assert Files.isSymbolicLink(versionFolder)

    def link = Files.readSymbolicLink(versionFolder).toString()
    assert link == directory
}

And(~'^the candidate "([^"]*)" version "([^"]*)" is already linked to "([^"]*)"$') { String candidate, String version, String folder ->
    def fileSystem = FileSystems.default

    def candidateFolder = "$gvmDir/$candidate" as File
    candidateFolder.mkdirs()

    def link = fileSystem.getPath("$gvmDir/$candidate/$version")
    def target = prepareLocalCandidateFolder(folder, candidate, version)

    Files.createSymbolicLink(link, target)
}

And(~'^I have configured "([^"]*)" to "([^"]*)"$') { String configName, String flag ->
    def configFile = new File("$gvmDir/etc/config")
    configFile.write "${configName}=${flag}"
}

private prepareCandidateFolder(String baseDir, String candidate, String version) {
    def directory = "$baseDir/$candidate/$version"
    prepareCandidateBinFolder directory, candidate, version
}

private prepareLocalCandidateFolder(String baseDir, String candidate, String version){
    prepareCandidateBinFolder baseDir, candidate, version
}

private prepareCandidateBinFolder(String folder, String candidate, String version) {
    def fileSystem = FileSystems.default

    def binFolderPath = fileSystem.getPath("$folder/bin")
    Files.createDirectories binFolderPath
    prepareCandidateExecutable binFolderPath, candidate, version

    return fileSystem.getPath("$folder")
}

private prepareCandidateExecutable(Path binFolder, String candidate, String version) {
    def candidateFile = new File("$binFolder/$candidate")
    candidateFile.write "echo ${candidate.capitalize()} Version: ${version}"
    candidateFile.executable = true
}