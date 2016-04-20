package io.codearte.accurest.builder

import io.codearte.accurest.dsl.GroovyDsl
import spock.lang.Specification

/**

 # TO CONSIDER
 
 - multiple messages can be sent out (for now let's focus on a single one)
 - tests thanks to stream-test-binder will get executed in single threaded mode (no need for awaitility)
 - do we need an explicit assertion of an input message (the one that enters the source?) or will
 it be done in the processor

 - let's support only JSON payload and headers taken from message ATM
 
 
 # TRIGGERING MESSAGES
 
 Triggering for the client side might be done via stub runner messaging module.

 ## JUNIT

 @ClassRule public static AccurestRule stubFinder = new AccurestRule()
  .repoRoot(repoRoot())
  .downloadStub("io.codearte.accurest.stubs", "loanIssuance")
  .downloadStub("io.codearte.accurest.stubs:fraudDetectionServer");

 ## SPRING

  @Autowired StubFinder stubFinder

 ## TRIGGERING EXAMPLES

  //test:

  // execute all triggers for all artifacts
   stubFinder.trigger()
  // execute all triggers named 'some_label' for all artifacts
   stubFinder.trigger("some_label")
  // execute all triggers named 'some_label' for the artifact in Ivy notation
   stubFinder.trigger("io.codearte.accurest.stubs:fraudDetectionServer", "some_label")
  // execute all triggers named 'some_label' for the artifact with id ...
   stubFinder.trigger("fraudDetectionServer", "some_label")

  if no label is provided then all triggers will get executed

 # CLIENT SIDE TEST EXAMPLE

 ## DSL

   GroovyDsl.make {
       label 'some_label'
    input {
           triggeredBy(execute('method()'))
       }
    outputMessage {
        onChannel('messageFrom')
        messageBody("book returned")
    }
   }

 ## TEST

 @Test
  public void run_some_test() {
	// given
    client.borrowABook();

    // when - sends a message to the messageFrom provided in the "outputMessage" section of the DSL
    stubFinder.trigger("client_returned_a_book");

    // then
    then(client).hasNoBooksBorrowed();
  }

 *
 * @author Marcin Grzejszczak
 */
class MessagingSpec extends Specification {

	// client side: must have a possibility to "trigger" sending of a message to the given messageFrom
	// server side: will run the method and await upon receiving message on the output messageFrom
	def "should generate tests triggered by a method"() {
		expect:
		GroovyDsl.make {
			label 'some_label'
			input {
				triggeredBy('bookReturnedTriggered()')
			}
			outputMessage {
				sentTo('channel')
				body('''{ "bookName" : "foo" }''')
				headers {
					header('BOOK-NAME', 'foo')
				}
			}
		}
	}

	// client side: if sends a message to input.messageFrom then message will be sent to output.messageFrom
	// server side: will send a message to input, verify the message contents and await upon receiving message on the output messageFrom
	def "should generate tests triggered by a message"() {
		expect:
		GroovyDsl.make {
			input {
				messageFrom('input')
				messageBody("some message")
				messageHeaders {
					header('key', 'value')
				}
			}
			outputMessage {
				sentTo('output')
				body('message')
				headers {
					header('anotherkey', 'anothervalue')
				}
			}
		}
	}

	// client side: if sends a message to input.messageFrom then message if matches will get consumed
	// server side: will send a message to input and verify the message contents
	def "should generate tests without destination, triggered by a message"() {
		expect:
		GroovyDsl.make {
			input {
				messageFrom('input')
				messageBody("some message")
				messageHeaders {
					header('key', 'value')
				}
			}
		}
	}
}
