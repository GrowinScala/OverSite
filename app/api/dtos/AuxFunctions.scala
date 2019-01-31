package api.dtos

object AuxFunctions {

  def convertEmailInfoToSender(emailInfoDTO: EmailInfoDTO, emailID: String): EmailInfoDTOSender = {

    EmailInfoDTOSender(emailID, emailInfoDTO.chatID, emailInfoDTO.fromAddress,
      emailInfoDTO.username, emailInfoDTO.header, emailInfoDTO.body, emailInfoDTO.dateOf)

  }

  def convertEmailInfoToShareSender(emailInfoDTO: EmailInfoDTO, shareId: String, emailID: String): ShareInfoDTOSender = {

    ShareInfoDTOSender(shareId, emailID, emailInfoDTO.chatID, emailInfoDTO.fromAddress,
      emailInfoDTO.username, emailInfoDTO.header, emailInfoDTO.body, emailInfoDTO.dateOf)
  }
}
