# cf-deploy.coffee
module.exports = (cfDeploy) ->
  env = cfDeploy.args.env

  {requiredParams} = cfDeploy.deployTools
  requiredParams(cfDeploy.args, 'env')

  envTail = if env == "d" then "-d" else ""

  appName = "support-triage-rotations-manager#{envTail}"

  appDomain = if env == "p" then "cf.local" else "mcf-np.local"

  console.log("************************** domain       = #{appDomain}")
  console.log("************************** api name     = #{appName}")

  buildpack: 'java_buildpack'
  cfBaseName: "#{appName}"
  deployable: "target/scala-2.12/support-triage-rotations-manager.war"
  deployer: cfDeploy.deployers.awsDeployment
  domain: "#{appDomain}"
  environment:
    APPLICATION_NAME: "#{appName}"
  route: "#{appName}"
  services: ["support-triage-rotations-manager-config#{envTail}"]
  memoryLimit: '1G'
  instances: 1

