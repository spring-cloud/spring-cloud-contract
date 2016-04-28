import io.codearte.accurest.dsl.GroovyDsl

GroovyDsl.make {
    label 'some_label'
    input {
        messageFrom('jms:input')
        messageBody([
                bookName: 'foo'
        ])
        messageHeaders {
            header('sample', 'header')
        }
    }
    outputMessage {
        sentTo('jms:output')
        body([
                bookName: 'foo'
        ])
        headers {
            header('BOOK-NAME', 'foo')
        }
    }
}