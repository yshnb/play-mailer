*Job opening: Scala programmer at Rhinofly*
-------------------------------------------
Each new project we start is being developed in Scala. Therefore, we are in need of a [Scala programmer](http://rhinofly.nl/vacatures/vacature-scala.html) who loves to write beautiful code. No more legacy projects or maintenance of old systems of which the original programmer is already six feet under. What we need is new, fresh code for awesome projects.

Are you the Scala programmer we are looking for? Take a look at the [job description](http://rhinofly.nl/vacatures/vacature-scala.html) (in Dutch) and give the Scala puzzle a try! Send us your solution and you will be invited for a job interview.
* * *

Scala mailer module for Play 2.2.x
=====================================================

Scala wrapper around java mail which allows you to send emails. The default configuration options exposed in Configuration work using Amazon SES SMTP

Installation
------------

``` scala
  val appDependencies = Seq(
    "play.modules.mailer" %% "play-mailer" % "2.1.2"
  )
  
  val main = PlayProject(appName, appVersion, appDependencies, mainLang = SCALA).settings(
    resolvers += "Rhinofly Internal Release Repository" at "http://maven-repository.rhinofly.net:8081/artifactory/libs-release-local"
  )
```

Configuration
-------------

`application.conf` should contain the following information:

``` scala
mail.failTo="failto+customer@company.org"

mail.host=email-smtp.us-east-1.amazonaws.com
mail.port=465

#only required if mail.auth=true (the default)
mail.username="Smtp username as generated by Amazon"
mail.password="Smtp password"
```

`application.conf` can additionally contain the following information:
``` scala
#default is smtps
mail.transport.protocol=smtp

#default is true
mail.auth=false
```

Usage
-----

### Creating an email

``` scala
  import play.modules.mailer._

  Email(
    subject = "Test mail",
    from = EmailAddress("Erik Westra sender", "ewestra@rhinofly.nl"),
    text = "text",
    htmlText = "htmlText",
    replyTo = None,
    recipients = List(Recipient(
    	RecipientType.TO, EmailAddress("Erik Westra recipient", "ewestra@rhinofly.nl"))),
    attachments = Seq.empty)
    
  // a more convenient way to create an email
  val email = Email(
    subject = "Test mail",
    from = EmailAddress("Erik Westra sender", "ewestra@rhinofly.nl"),
    text = "text",
    htmlText = "htmlText")
    .to("Erik Westra TO", "ewestra+to@rhinofly.nl")
    .cc("Erik Westra CC", "ewestra+cc@rhinofly.nl")
    .bcc("Erik Westra BCC", "ewestra+bcc@rhinofly.nl")
    .replyTo("Erik Westra REPLY_TO", "ewestra+replyTo@rhinofly.nl")
    .withAttachments(
    	Attachment("attachment1", Array[Byte](0, 1), "application/octet-stream"),
    	Attachment("attachment2", Array[Byte](0, 1), "application/octet-stream", Disposition.Inline))
```

### Sending an email synchronously

``` scala
  import play.modules.mailer._
  
  val result:Try[Unit] = Mailer.sendEmail(email)
  
  result match {
    case Success(_) => 
    	//mail sent successfully
    case Failure(SendEmailException(email, cause)) => 
    	//failed to send email, cause provides more information 
    case Failure(SendEmailTransportCloseException(None, cause)) =>
        //failed to close the connection, no email was sent
    case Failure(SendEmailTransportCloseException(Some(Success(_)), cause)) =>
        //failed to close the connection, the email was sent
    case Failure(SendEmailTransportCloseException(Some(Failure(SendEmailException(email, cause1)), cause2)) =>
        //failed to close the connection, the email was not sent
  }
  
```

### Sending multiple emails synchronously

``` scala
  import play.modules.mailer._
  
  val result:Try[Seq[Try[Unit]]] = Mailer.sendEmails(email1, email2)
  
  result match {
    case Success(results) =>
      results.foreach {
        case Success(_) => 
          //mail sent successfully
        case Failure(SendEmailException(email, cause)) =>
          //failed to send email, cause provides more information
      }
    case Failure(SendEmailsTransportCloseException(None, cause)) =>
      //failed to close the connection, no email was sent
    case Failure(SendEmailsTransportCloseException(Some(Seq(Success(_), Failure(SendEmailException(email, cause1))), cause2)) =>
      //failed to close the connection, one of the emails was sent
  }
```

### Sending mail asynchonously

``` scala
  import play.modules.mailer._

  val result:Future[Unit] = AsyncMailer.sendEmail(email)
  
  result
    .map { unit =>
      // mail sent successfully
  }
  .recover {
    case SendEmailException(email, cause) => 
      // problem sending email
    case SendEmailTransportCloseException(result, cause) => 
      // problem closing connection
  }
```

### Sending mails asynchonously

``` scala
  import play.modules.mailer._

  val result:Future[Seq[Try[Unit]]] = AsyncMailer.sendEmails(email)
    
  result
    .map { results =>
      results.foreach {
        case Success(_) => 
          //mail sent successfully
        case Failure(SendEmailException(email, cause)) =>
          //failed to send email, cause provides more information
      }
  	}
    .recover {
      case SendEmailException(email, cause) => 
        // problem sending email
      case SendEmailTransportCloseException(result, cause) => 
        // problem closing connection
    }
```
