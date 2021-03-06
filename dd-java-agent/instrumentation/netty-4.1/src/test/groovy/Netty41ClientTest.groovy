// Modified by SignalFx
import datadog.trace.api.DDSpanTypes
import datadog.opentracing.DDSpan
import datadog.trace.instrumentation.netty41.NettyUtils
import datadog.trace.agent.test.base.HttpClientTest
import datadog.trace.api.Trace
import datadog.trace.bootstrap.instrumentation.api.Tags
import datadog.trace.instrumentation.netty41.client.HttpClientTracingHandler
import datadog.trace.instrumentation.netty41.client.NettyHttpClientDecorator
import io.netty.channel.AbstractChannel
import io.netty.channel.Channel
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInitializer
import io.netty.channel.embedded.EmbeddedChannel
import io.netty.handler.codec.http.HttpClientCodec
import org.asynchttpclient.AsyncCompletionHandler
import org.asynchttpclient.AsyncHttpClient
import org.asynchttpclient.DefaultAsyncHttpClientConfig
import org.asynchttpclient.Response
import spock.lang.Retry
import spock.lang.Shared
import spock.lang.Timeout

import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

import static datadog.trace.agent.test.server.http.TestHttpServer.httpServer
import static datadog.trace.agent.test.utils.PortUtils.UNUSABLE_PORT
import static datadog.trace.agent.test.utils.TraceUtils.basicSpan
import static datadog.trace.agent.test.utils.TraceUtils.runUnderTrace
import static org.asynchttpclient.Dsl.asyncHttpClient

@Retry
@Timeout(5)
class Netty41ClientTest extends HttpClientTest {

  @Shared
  def clientConfig = DefaultAsyncHttpClientConfig.Builder.newInstance().setRequestTimeout(TimeUnit.SECONDS.toMillis(10).toInteger())
  @Shared
  AsyncHttpClient asyncHttpClient = asyncHttpClient(clientConfig)

  @Override
  int doRequest(String method, URI uri, Map<String, String> headers, Closure callback) {
    def methodName = "prepare" + method.toLowerCase().capitalize()
    def requestBuilder = asyncHttpClient."$methodName"(uri.toString())
    headers.each { requestBuilder.setHeader(it.key, it.value) }
    def response = requestBuilder.execute(new AsyncCompletionHandler() {
      @Override
      Object onCompleted(Response response) throws Exception {
        callback?.call()
        return response
      }
    }).get()
    blockUntilChildSpansFinished(1)
    return response.statusCode
  }

  @Override
  String component() {
    return NettyHttpClientDecorator.DECORATE.component()
  }

  @Override
  String expectedOperationName() {
    return "netty.client.request"
  }

  @Override
  boolean testRedirects() {
    false
  }

  @Override
  boolean testConnectionFailure() {
    false
  }

  @Override
  boolean testRemoteConnection() {
    return false
  }

  def "connection error (unopened port)"() {
    given:
    def uri = new URI("http://localhost:$UNUSABLE_PORT/")

    when:
    runUnderTrace("parent") {
      doRequest(method, uri)
    }

    then:
    def ex = thrown(Exception)
    ex.cause instanceof ConnectException
    def thrownException = ex instanceof ExecutionException ? ex.cause : ex

    and:
    assertTraces(1) {
      trace(0, 2) {
        basicSpan(it, 0, "parent", null, thrownException)

        span(1) {
          operationName "netty.connect"
          resourceName "netty.connect"
          childOf span(0)
          errored true
          tags {
            "$Tags.COMPONENT" "netty"
            errorTags AbstractChannel.AnnotatedConnectException, "Connection refused: localhost/127.0.0.1:$UNUSABLE_PORT"
            defaultTags()
          }
        }
      }
    }

    where:
    method = "GET"
  }

  def "when a handler is added to the netty pipeline we add our tracing handler"() {
    setup:
    def channel = new EmbeddedChannel()
    def pipeline = channel.pipeline()

    when:
    pipeline.addLast("name", new HttpClientCodec())

    then:
    // The first one returns the removed tracing handler
    pipeline.remove(HttpClientTracingHandler.getName()) != null
  }

  def "when a handler is added to the netty pipeline we add ONLY ONE tracing handler"() {
    setup:
    def channel = new EmbeddedChannel()
    def pipeline = channel.pipeline()

    when:
    pipeline.addLast("name", new HttpClientCodec())
    // The first one returns the removed tracing handler
    pipeline.remove(HttpClientTracingHandler.getName())
    // There is only one
    pipeline.remove(HttpClientTracingHandler.getName()) == null

    then:
    thrown NoSuchElementException
  }

  def "handlers of different types can be added"() {
    setup:
    def channel = new EmbeddedChannel()
    def pipeline = channel.pipeline()

    when:
    pipeline.addLast("some_handler", new SimpleHandler())
    pipeline.addLast("a_traced_handler", new HttpClientCodec())

    then:
    // The first one returns the removed tracing handler
    null != pipeline.remove(HttpClientTracingHandler.getName())
    null != pipeline.remove("some_handler")
    null != pipeline.remove("a_traced_handler")
  }

  def "calling pipeline.addLast methods that use overloaded methods does not cause infinite loop"() {
    setup:
    def channel = new EmbeddedChannel()

    when:
    channel.pipeline().addLast(new SimpleHandler(), new OtherSimpleHandler())

    then:
    null != channel.pipeline().remove('Netty41ClientTest$SimpleHandler#0')
    null != channel.pipeline().remove('Netty41ClientTest$OtherSimpleHandler#0')
  }

  def "when a traced handler is added from an initializer we still detect it and add our channel handlers"() {
    // This test method replicates a scenario similar to how reactor 0.8.x register the `HttpClientCodec` handler
    // into the pipeline.

    setup:
    def channel = new EmbeddedChannel()

    when:
    channel.pipeline().addLast(new TracedHandlerFromInitializerHandler())

    then:
    null != channel.pipeline().remove("added_in_initializer")
    null != channel.pipeline().remove(HttpClientTracingHandler.getName())
  }

  def "request with trace annotated method"() {
    given:
    def annotatedClass = new AnnotatedClass()

    when:
    def status = runUnderTrace("parent") {
      annotatedClass.makeRequestUnderTrace(method)
    }

    then:
    status == 200
    assertTraces(2) {
      server.distributedRequestTrace(it, 0, trace(1).last())
      trace(1, size(3)) {
        basicSpan(it, 0, "parent")
        span(1) {
          childOf((DDSpan) span(0))
          serviceName "unnamed-java-service"
          operationName "trace.annotation"
          resourceName "AnnotatedClass.makeRequestUnderTrace"
          errored false
          tags {
            "$Tags.COMPONENT" "trace"
            defaultTags()
          }
        }
        clientSpan(it, 2, span(1), method)
      }
    }

    where:
    method << BODY_METHODS
  }

  class AnnotatedClass {
    @Trace
    int makeRequestUnderTrace(String method) {
      return doRequest(method, server.address.resolve("/success"))
    }
  }

  class SimpleHandler implements ChannelHandler {
    @Override
    void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    }
  }

  class OtherSimpleHandler implements ChannelHandler {
    @Override
    void handlerAdded(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
    }

    @Override
    void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    }
  }

  class TracedHandlerFromInitializerHandler extends ChannelInitializer<Channel> implements ChannelHandler {
    @Override
    protected void initChannel(Channel ch) throws Exception {
      // This replicates how reactor 0.8.x add the HttpClientCodec
      ch.pipeline().addLast("added_in_initializer", new HttpClientCodec())
    }
  }

  def "test #statusCode statusCode rewrite #rewrite"() {
    setup:
    def property = "signalfx.${NettyUtils.NETTY_REWRITTEN_CLIENT_STATUS_PREFIX}$statusCode"
    System.getProperties().setProperty(property, "$rewrite")

    server = httpServer {
      handlers {
        post("/post") {
          int sc = request.headers.get("X-Status-Code").toInteger()
          response.status(sc).send("Received")
        }
      }
    }

    def resStatusCode
    runUnderTrace("parent") {
      resStatusCode = doRequest("POST", server.address.resolve("/post"), ["X-Status-Code" : statusCode.toString()])
    }

    expect:
    resStatusCode == statusCode

    and:
    assertTraces(1) {
      trace(0, 2) {
        span(0) {
          operationName "parent"
          parent()
        }
        span(1) {
          serviceName "unnamed-java-service"
          operationName "netty.client.request"
          resourceName "/post"
          spanType DDSpanTypes.HTTP_CLIENT
          childOf span(0)
          errored error
          tags {
            "$Tags.COMPONENT" "netty-client"
            "$Tags.HTTP_METHOD" "POST"
            if (rewrite) {
              "$Tags.HTTP_STATUS" null
              "$NettyUtils.ORIG_HTTP_STATUS" statusCode
            } else {
              "$Tags.HTTP_STATUS" statusCode
            }
            "$Tags.HTTP_URL" "$server.address/post"
            "$Tags.PEER_HOSTNAME" "localhost"
            "$Tags.PEER_HOST_IPV4" "127.0.0.1"
            "$Tags.PEER_PORT" Integer
            "$Tags.SPAN_KIND" Tags.SPAN_KIND_CLIENT
            if (error) {
              tag("error", true)
            }
            defaultTags()
          }
        }
      }
    }

    and:
    server.lastRequest.headers.get("x-b3-traceid") == String.format("%016x", TEST_WRITER.get(0).get(1).traceId)
    server.lastRequest.headers.get("x-b3-spanid") == String.format("%016x", TEST_WRITER.get(0).get(1).spanId)

    where:
    statusCode | error | rewrite
    200        | false | false
    200        | false | true
    500        | true  | false
    500        | false | true
    502        | true  | false
    502        | false | true
    503        | true  | false
    503        | false | true
  }
}
