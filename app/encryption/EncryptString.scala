package encryption
import java.security.MessageDigest

class EncryptString(toEncrypt: String) {

  val original = toEncrypt
  val md = MessageDigest.getInstance("MD5")
  md.update(original.getBytes)
  val digest = md.digest
  val result: StringBuffer = new StringBuffer
  for (b <- digest) {
    result.append(b & 0xff)
  }

}
