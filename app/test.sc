import java.text.SimpleDateFormat
import java.util.Date

import encryption.EncryptString
import slick.lifted.Rep

val encrypt = new EncryptString("")
encrypt.result.toString
System.currentTimeMillis/1000

val yourmilliseconds = System.currentTimeMillis();
val sdf = new SimpleDateFormat("MMM dd,yyyy HH:mm");
val resultdate = new Date(yourmilliseconds + 3600000 );
System.out.println(sdf.format(resultdate));
