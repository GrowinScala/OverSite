package encryption

import java.security.MessageDigest

/**
 * Class that receives a password as a String and encrypts it
 *
 * @param toEncrypt
 */
class EncryptString(toEncrypt: String, targetAlgorithm: String) {
  /**
   * Gets an instance of target algorithm
   */
  val md: MessageDigest = MessageDigest.getInstance(targetAlgorithm)

  /**
   * Converts String into bytes
   */
  md.update(toEncrypt.getBytes)

  /**
   * Gets digest
   */
  val digest: Array[Byte] = md.digest
  val result: StringBuffer = new StringBuffer
  for (b <- digest) {
    result.append(b & 0xff)
  }

}
