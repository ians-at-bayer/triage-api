package app.web.v1.setup

case class SetupDTO(appBaseUrl: String,
                    rotationConfig: RotationSetupDTO,
                    slackConfig: SlackSetupDTO,
                    people: Array[PersonSetupDTO])
