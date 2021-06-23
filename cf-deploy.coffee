# cf-deploy.coffee
# This app only lives in NP
module.exports = (cfDeploy) ->
  appName = "support-triage-manager-api"
  buildpack: 'java_buildpack'
  cfBaseName: "#{appName}"
  deployable: "target/scala-2.12/support-triage-rotations-manager.war"
  deployer: cfDeploy.deployers.awsDeployment
  domain: "mcf-np.local"
  environment:
    APPLICATION_NAME: "#{appName}"
  route: "#{appName}"
  services: ["support-triage-manager-config"]
  memoryLimit: '1G'
  instances: 1

