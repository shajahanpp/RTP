#!/usr/bin/env groovy
/**
 * Custom Properties
 */

def call(language, projectEnv, sourceRepositoryTypeRef) {
	return [
		[
			type: "boolean",
			name: 'openshiftCreateProject',
			showWhen: projectEnv != "init",
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'openshiftCreateBitBucketSecret',
			showWhen: projectEnv ==~ /dev|buildrc|rel/,
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef),
		],[ 
			type: "boolean",
			name: 'mavenBuild',
			showWhen: projectEnv ==~ /dev|buildrc|rel/ && language.contains("java"),
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef),
		],[
			type: "boolean",
			name: 'mavenUnitTest',
			showWhen: projectEnv ==~ /dev|buildrc|rel/ && language.contains("java"),
			defaultValue: false,
		],[
			type: "boolean",
			name: 'mavenSonar',
			showWhen: projectEnv ==~ /dev|buildrc|rel/ && language.contains("java"),
			defaultValue: false,
		],[
			type: "boolean",
			name: 'veracode',
			showWhen: projectEnv ==~ /dev|buildrc|rel/ && language.contains("java"),
			defaultValue: false,
		],[
			type: "boolean",
			name: 'mavenDeploy',
			showWhen: projectEnv ==~ /dev|buildrc|rel/ && language.contains("java"),
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef),
		],[
			type: "boolean",
			name: 'openshiftCreateApp',
			showWhen: projectEnv != "init",
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'openshiftCreateArtifactorySecret',
			showWhen: projectEnv ==~ /dev|buildrc|rel/,
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'openshiftRetagBuild',
			showWhen: true, 
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'openshiftBuild',
			showWhen: projectEnv ==~ /dev|buildrc|rel/,
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'openshiftJiraRelease',
			showWhen: projectEnv == "rel",
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'openshiftSecurityImage',
			showWhen: projectEnv == "rel",
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'openshiftPromotionImageStream',
			showWhen: projectEnv ==~ /test|uat|prod/,
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'openshiftRetagDeploy',
			showWhen: projectEnv ==~ /dev|test|uat|prod/,
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'openshiftPromotionRetagDeploy',
			showWhen: projectEnv ==~ /test|uat|prod/,
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			showWhen: projectEnv != "init" && language != "hygieia" && projectEnv ==~ /dev|test|uat|prod/,
			name: 'openshiftCreateConfigMap',
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'openshiftDeploy',
			showWhen: projectEnv != "init" && projectEnv ==~ /dev|test|uat|prod/,
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'openshiftCreateRoute',
			showWhen: projectEnv != "init" && language != "hygieia" && projectEnv ==~ /dev|test|uat|prod/,
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'openshiftCreateHPA',
			showWhen: projectEnv != "init" && language != "hygieia" && projectEnv ==~ /dev|test|uat|prod/,
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'sysdigMonitoring',
			showWhen: projectEnv == "uat" || projectEnv == "prod",
			defaultValue: devopspaasParametersLoader.defaultValueForNonFeatures(sourceRepositoryTypeRef)
		],[
			type: "boolean",
			name: 'debug',
			showWhen: true,
			description: 'Switch on debug mode logger',
			defaultValue: false
		],[
			type: "hidden",
			name: 'TRIGGER_REF',
			showWhen: true,
			defaultValue: "",
		],[
			type: "hidden",
			name: 'PULL_REQUEST_FROM_BRANCH',
			showWhen: true,
			defaultValue: "",
		],[
			type: "hidden",
			name: 'PULL_REQUEST_ID',
			showWhen: true,
			defaultValue: "",
		]
	]
}
return this
