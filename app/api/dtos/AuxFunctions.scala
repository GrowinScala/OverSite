package api.dtos

object AuxFunctions {

  def convertEmailInfoToSender(emailInfoDTO: EmailInfoDTO, emailID: String): EmailInfoDTOSender = {

    EmailInfoDTOSender(emailID, emailInfoDTO.chatID, emailInfoDTO.fromAddress,
      emailInfoDTO.username, emailInfoDTO.header, emailInfoDTO.body, emailInfoDTO.dateOf)

  }

  def convertEmailInfoToShareSender(emailInfoDTO: EmailInfoDTO, emailID: String, shareId: String): ShareInfoDTOSender = {

    ShareInfoDTOSender(shareId, emailID, emailInfoDTO.chatID, emailInfoDTO.fromAddress,
      emailInfoDTO.username, emailInfoDTO.header, emailInfoDTO.body, emailInfoDTO.dateOf)
  }
}
