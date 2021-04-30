package app.web.v1.setup

case class SetupDTO(teamName: String,
                    rotationConfig: RotationSetupDTO,
                    slackConfig: SlackSetupDTO,
                    people: Array[PersonSetupDTO])
