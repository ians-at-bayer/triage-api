package app.web.v1.setup

case class SetupDTO(teamName: String,
                    rotationConfig: RotationSetupDTO,
                    teamsConfig: TeamsSetupDTO,
                    people: Array[PersonSetupDTO])
