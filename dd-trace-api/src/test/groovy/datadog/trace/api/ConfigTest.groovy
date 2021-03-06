// Modified by SignalFx
package datadog.trace.api

import datadog.trace.util.test.DDSpecification
import org.junit.Rule
import org.junit.contrib.java.lang.system.EnvironmentVariables
import org.junit.contrib.java.lang.system.RestoreSystemProperties

import static datadog.trace.api.Config.DEFAULT_REDIS_CAPTURE_COMMAND_ARGUMENTS
import static datadog.trace.api.Config.ENDPOINT_URL
import static datadog.trace.api.Config.AGENT_HOST
import static datadog.trace.api.Config.AGENT_PATH
import static datadog.trace.api.Config.AGENT_PORT_LEGACY
import static datadog.trace.api.Config.AGENT_UNIX_DOMAIN_SOCKET
import static datadog.trace.api.Config.DB_STATEMENT_MAX_LENGTH
import static datadog.trace.api.Config.DEFAULT_DB_STATEMENT_MAX_LENGTH
import static datadog.trace.api.Config.API_KEY
import static datadog.trace.api.Config.API_KEY_FILE
import static datadog.trace.api.Config.CONFIGURATION_FILE
import static datadog.trace.api.Config.DB_CLIENT_HOST_SPLIT_BY_INSTANCE
import static datadog.trace.api.Config.DEFAULT_JMX_FETCH_STATSD_PORT
import static datadog.trace.api.Config.DEFAULT_KAFKA_ATTEMPT_PROPAGATION
import static datadog.trace.api.Config.GLOBAL_TAGS
import static datadog.trace.api.Config.HEADER_TAGS
import static datadog.trace.api.Config.HOST_TAG
import static datadog.trace.api.Config.HTTP_CLIENT_ERROR_STATUSES
import static datadog.trace.api.Config.HTTP_CLIENT_HOST_SPLIT_BY_DOMAIN
import static datadog.trace.api.Config.HTTP_SERVER_ERROR_STATUSES
import static datadog.trace.api.Config.JMX_FETCH_CHECK_PERIOD
import static datadog.trace.api.Config.JMX_FETCH_ENABLED
import static datadog.trace.api.Config.JMX_FETCH_METRICS_CONFIGS
import static datadog.trace.api.Config.JMX_FETCH_REFRESH_BEANS_PERIOD
import static datadog.trace.api.Config.JMX_FETCH_STATSD_HOST
import static datadog.trace.api.Config.JMX_FETCH_STATSD_PORT
import static datadog.trace.api.Config.JMX_TAGS
import static datadog.trace.api.Config.KAFKA_ATTEMPT_PROPAGATION
import static datadog.trace.api.Config.REDIS_CAPTURE_COMMAND_ARGUMENTS
import static datadog.trace.api.Config.TRACING_LIBRARY_KEY
import static datadog.trace.api.Config.TRACING_LIBRARY_VALUE
import static datadog.trace.api.Config.TRACING_VERSION_KEY
import static datadog.trace.api.Config.TRACING_VERSION_VALUE
import static datadog.trace.api.Config.LANGUAGE_TAG_KEY
import static datadog.trace.api.Config.LANGUAGE_TAG_VALUE
import static datadog.trace.api.Config.PARTIAL_FLUSH_MIN_SPANS
import static datadog.trace.api.Config.PREFIX
import static datadog.trace.api.Config.PRIORITY_SAMPLING
import static datadog.trace.api.Config.PROFILING_API_KEY_FILE_OLD
import static datadog.trace.api.Config.PROFILING_API_KEY_FILE_VERY_OLD
import static datadog.trace.api.Config.PROFILING_ENABLED
import static datadog.trace.api.Config.PROFILING_PROXY_HOST
import static datadog.trace.api.Config.PROFILING_PROXY_PASSWORD
import static datadog.trace.api.Config.PROFILING_PROXY_PORT
import static datadog.trace.api.Config.PROFILING_PROXY_USERNAME
import static datadog.trace.api.Config.PROFILING_START_DELAY
import static datadog.trace.api.Config.PROFILING_START_FORCE_FIRST
import static datadog.trace.api.Config.PROFILING_TAGS
import static datadog.trace.api.Config.PROFILING_TEMPLATE_OVERRIDE_FILE
import static datadog.trace.api.Config.PROFILING_UPLOAD_COMPRESSION
import static datadog.trace.api.Config.PROFILING_UPLOAD_PERIOD
import static datadog.trace.api.Config.PROFILING_UPLOAD_TIMEOUT
import static datadog.trace.api.Config.PROFILING_URL
import static datadog.trace.api.Config.PROPAGATION_STYLE_EXTRACT
import static datadog.trace.api.Config.PROPAGATION_STYLE_INJECT
import static datadog.trace.api.Config.RUNTIME_CONTEXT_FIELD_INJECTION
import static datadog.trace.api.Config.SERVICE
import static datadog.trace.api.Config.SIGNALFX_PREFIX
import static datadog.trace.api.Config.SERVICE_MAPPING
import static datadog.trace.api.Config.SERVICE_NAME
import static datadog.trace.api.Config.SERVICE_TAG
import static datadog.trace.api.Config.SITE
import static datadog.trace.api.Config.SPAN_TAGS
import static datadog.trace.api.Config.SPLIT_BY_TAGS
import static datadog.trace.api.Config.TAGS
import static datadog.trace.api.Config.TRACE_AGENT_PORT
import static datadog.trace.api.Config.TRACE_ENABLED
import static datadog.trace.api.Config.TRACE_RATE_LIMIT
import static datadog.trace.api.Config.TRACE_REPORT_HOSTNAME
import static datadog.trace.api.Config.TRACE_RESOLVER_ENABLED
import static datadog.trace.api.Config.USE_B3_PROPAGATION
import static datadog.trace.api.Config.HEALTH_METRICS_ENABLED
import static datadog.trace.api.Config.HEALTH_METRICS_STATSD_HOST
import static datadog.trace.api.Config.HEALTH_METRICS_STATSD_PORT
import static datadog.trace.api.Config.RECORDED_VALUE_MAX_LENGTH
import static datadog.trace.api.Config.DEFAULT_RECORDED_VALUE_MAX_LENGTH
import static datadog.trace.api.Config.TRACE_SAMPLE_RATE
import static datadog.trace.api.Config.TRACE_SAMPLING_OPERATION_RULES
import static datadog.trace.api.Config.TRACE_SAMPLING_SERVICE_RULES
import static datadog.trace.api.Config.WRITER_TYPE

class ConfigTest extends DDSpecification {
  @Rule
  public final RestoreSystemProperties restoreSystemProperties = new RestoreSystemProperties()
  @Rule
  public final EnvironmentVariables environmentVariables = new EnvironmentVariables()

  private static final DD_API_KEY_ENV = "DD_API_KEY"
  private static final DD_SERVICE_NAME_ENV = "DD_SERVICE_NAME"
  private static final DD_TRACE_ENABLED_ENV = "DD_TRACING_ENABLED"
  private static final DD_WRITER_TYPE_ENV = "DD_WRITER_TYPE"
  private static final DD_SERVICE_MAPPING_ENV = "DD_SERVICE_MAPPING"
  private static final DD_TAGS_ENV = "DD_TAGS"
  private static final DD_ENV_ENV = "DD_ENV"
  private static final DD_VERSION_ENV = "DD_VERSION"
  private static final DD_GLOBAL_TAGS_ENV = "DD_TRACE_GLOBAL_TAGS"
  private static final DD_SPAN_TAGS_ENV = "SIGNALFX_SPAN_TAGS"
  private static final DD_HEADER_TAGS_ENV = "DD_TRACE_HEADER_TAGS"
  private static final DD_JMX_TAGS_ENV = "DD_TRACE_JMX_TAGS"
  private static final DD_PROPAGATION_STYLE_EXTRACT = "DD_PROPAGATION_STYLE_EXTRACT"
  private static final DD_PROPAGATION_STYLE_INJECT = "DD_PROPAGATION_STYLE_INJECT"
  private static final DD_JMXFETCH_METRICS_CONFIGS_ENV = "DD_JMXFETCH_METRICS_CONFIGS"
  private static final DD_TRACE_AGENT_PORT_ENV = "DD_TRACE_AGENT_PORT"
  private static final DD_AGENT_PORT_LEGACY_ENV = "DD_AGENT_PORT"
  private static final DD_TRACE_REPORT_HOSTNAME = "DD_TRACE_REPORT_HOSTNAME"
  private static final SIGNALFX_DB_STATEMENT_MAX_LENGTH = "SIGNALFX_DB_STATEMENT_MAX_LENGTH"
  private static final SIGNALFX_KAFKA_ATTEMPT_PROPAGATION_ENV = "SIGNALFX_INSTRUMENTATION_KAFKA_ATTEMPT_PROPAGATION"
  private static final SIGNALFX_REDIS_CAPTURE_COMMAND_ARGUMENTS = "SIGNALFX_INSTRUMENTATION_REDIS_CAPTURE_COMMAND_ARGUMENTS"
  private static final SIGNALFX_RECORDED_VALUE_MAX_LENGTH = "SIGNALFX_RECORDED_VALUE_MAX_LENGTH"

  private static final DD_PROFILING_API_KEY_OLD_ENV = "DD_PROFILING_API_KEY"
  private static final DD_PROFILING_API_KEY_VERY_OLD_ENV = "DD_PROFILING_APIKEY"
  private static final DD_PROFILING_TAGS_ENV = "DD_PROFILING_TAGS"
  private static final DD_PROFILING_PROXY_PASSWORD_ENV = "DD_PROFILING_PROXY_PASSWORD"

  def "verify defaults"() {
    when:
    Config config = provider()

    then:
    config.apiKey == null
    config.site == Config.DEFAULT_SITE
    config.serviceName == "unnamed-java-service"
    config.traceEnabled == true
    config.writerType == "DDAgentWriter"
    config.apiType == "ZipkinV2"
    config.useB3Propagation == true
    config.getAgentHost() == "localhost"
    config.getAgentPort() == 9080
    config.getAgentPath() == "/v1/trace"
    config.getAgentUseHTTPS() == false
    config.endpointUrl.toString() == "http://localhost:9080/v1/trace"
    config.agentUnixDomainSocket == null
    config.prioritySamplingEnabled == false
    config.traceResolverEnabled == true
    config.serviceMapping == [:]
    config.mergedSpanTags == [:]
    config.mergedJmxTags == [(SERVICE): config.serviceName]
    config.getLocalRootSpanTags() == [(TRACING_LIBRARY_KEY):TRACING_LIBRARY_VALUE, (TRACING_VERSION_KEY):TRACING_VERSION_VALUE]
    config.headerTags == [:]
    config.httpServerErrorStatuses == (500..599).toSet()
    config.httpClientErrorStatuses == (500..599).toSet()
    config.httpClientSplitByDomain == false
    config.dbClientSplitByInstance == false
    config.splitByTags == [].toSet()
    config.partialFlushMinSpans == 1000
    config.reportHostName == false
    config.runtimeContextFieldInjection == true
    config.propagationStylesToExtract.toList() == [Config.PropagationStyle.B3]
    config.propagationStylesToInject.toList() == [Config.PropagationStyle.B3]
    config.jmxFetchEnabled == false
    config.jmxFetchMetricsConfigs == []
    config.jmxFetchCheckPeriod == null
    config.jmxFetchRefreshBeansPeriod == null
    config.jmxFetchStatsdHost == null
    config.jmxFetchStatsdPort == DEFAULT_JMX_FETCH_STATSD_PORT

    config.healthMetricsEnabled == false
    config.healthMetricsStatsdHost == null
    config.healthMetricsStatsdPort == null

    config.profilingEnabled == false
    config.profilingUrl == null
    config.mergedProfilingTags == [(HOST_TAG): config.getHostName(), (SERVICE_TAG): config.serviceName, (LANGUAGE_TAG_KEY): LANGUAGE_TAG_VALUE]
    config.profilingStartDelay == 10
    config.profilingStartForceFirst == false
    config.profilingUploadPeriod == 60
    config.profilingTemplateOverrideFile == null
    config.profilingUploadTimeout == 30
    config.profilingProxyHost == null
    config.profilingProxyPort == Config.DEFAULT_PROFILING_PROXY_PORT
    config.profilingProxyUsername == null
    config.profilingProxyPassword == null

    config.toString().contains("unnamed-java-service")
    config.dbStatementMaxLength == DEFAULT_DB_STATEMENT_MAX_LENGTH
    config.kafkaAttemptPropagation == DEFAULT_KAFKA_ATTEMPT_PROPAGATION
    config.redisCaptureCommandArguments == DEFAULT_REDIS_CAPTURE_COMMAND_ARGUMENTS
    config.recordedValueMaxLength == DEFAULT_RECORDED_VALUE_MAX_LENGTH

    where:
    provider << [{ new Config() }, { Config.get() }, {
      def props = new Properties()
      props.setProperty("something", "unused")
      Config.get(props)
    }]
  }

  def "specify overrides via properties"() {
    setup:
    def prop = new Properties()
    prop.setProperty(API_KEY, "new api key")
    prop.setProperty(SITE, "new site")
    prop.setProperty(SERVICE_NAME, "something else")
    prop.setProperty(TRACE_ENABLED, "false")
    prop.setProperty(WRITER_TYPE, "LoggingWriter")
    prop.setProperty(AGENT_HOST, "somehost")
    prop.setProperty(TRACE_AGENT_PORT, "123")
    prop.setProperty(AGENT_UNIX_DOMAIN_SOCKET, "somepath")
    prop.setProperty(AGENT_PORT_LEGACY, "456")
    prop.setProperty(PRIORITY_SAMPLING, "false")
    prop.setProperty(TRACE_RESOLVER_ENABLED, "false")
    prop.setProperty(SERVICE_MAPPING, "a:1")
    prop.setProperty(GLOBAL_TAGS, "b:2")
    prop.setProperty(SPAN_TAGS, "c:3")
    prop.setProperty(JMX_TAGS, "d:4")
    prop.setProperty(HEADER_TAGS, "e:5")
    prop.setProperty(HTTP_SERVER_ERROR_STATUSES, "123-456,457,124-125,122")
    prop.setProperty(HTTP_CLIENT_ERROR_STATUSES, "111")
    prop.setProperty(HTTP_CLIENT_HOST_SPLIT_BY_DOMAIN, "true")
    prop.setProperty(DB_CLIENT_HOST_SPLIT_BY_INSTANCE, "true")
    prop.setProperty(SPLIT_BY_TAGS, "some.tag1,some.tag2,some.tag1")
    prop.setProperty(PARTIAL_FLUSH_MIN_SPANS, "15")
    prop.setProperty(TRACE_REPORT_HOSTNAME, "true")
    prop.setProperty(RUNTIME_CONTEXT_FIELD_INJECTION, "false")
    prop.setProperty(PROPAGATION_STYLE_EXTRACT, "Datadog, B3")
    prop.setProperty(PROPAGATION_STYLE_INJECT, "B3, Datadog")
    prop.setProperty(JMX_FETCH_ENABLED, "false")
    prop.setProperty(JMX_FETCH_METRICS_CONFIGS, "/foo.yaml,/bar.yaml")
    prop.setProperty(JMX_FETCH_CHECK_PERIOD, "100")
    prop.setProperty(JMX_FETCH_REFRESH_BEANS_PERIOD, "200")
    prop.setProperty(JMX_FETCH_STATSD_HOST, "statsd host")
    prop.setProperty(JMX_FETCH_STATSD_PORT, "321")
    prop.setProperty(DB_STATEMENT_MAX_LENGTH, "100")
    prop.setProperty(KAFKA_ATTEMPT_PROPAGATION, "false")
    prop.setProperty(REDIS_CAPTURE_COMMAND_ARGUMENTS, "false")
    prop.setProperty(RECORDED_VALUE_MAX_LENGTH, "10")
    prop.setProperty(HEALTH_METRICS_ENABLED, "false")
    prop.setProperty(HEALTH_METRICS_STATSD_HOST, "metrics statsd host")
    prop.setProperty(HEALTH_METRICS_STATSD_PORT, "654")
    prop.setProperty(TRACE_SAMPLING_SERVICE_RULES, "a:1")
    prop.setProperty(TRACE_SAMPLING_OPERATION_RULES, "b:1")
    prop.setProperty(TRACE_SAMPLE_RATE, ".5")
    prop.setProperty(TRACE_RATE_LIMIT, "200")

    prop.setProperty(PROFILING_ENABLED, "true")
    prop.setProperty(PROFILING_URL, "new url")
    prop.setProperty(PROFILING_TAGS, "f:6,host:test-host")
    prop.setProperty(PROFILING_START_DELAY, "1111")
    prop.setProperty(PROFILING_START_FORCE_FIRST, "true")
    prop.setProperty(PROFILING_UPLOAD_PERIOD, "1112")
    prop.setProperty(PROFILING_TEMPLATE_OVERRIDE_FILE, "/path")
    prop.setProperty(PROFILING_UPLOAD_TIMEOUT, "1116")
    prop.setProperty(PROFILING_UPLOAD_COMPRESSION, "off")
    prop.setProperty(PROFILING_PROXY_HOST, "proxy-host")
    prop.setProperty(PROFILING_PROXY_PORT, "1118")
    prop.setProperty(PROFILING_PROXY_USERNAME, "proxy-username")
    prop.setProperty(PROFILING_PROXY_PASSWORD, "proxy-password")

    when:
    Config config = Config.get(prop)

    then:
    config.apiKey == "new api key" // we can still override via internal properties object
    config.site == "new site"
    config.serviceName == "something else"
    config.traceEnabled == false
    config.writerType == "LoggingWriter"
    config.agentHost == "somehost"
    config.agentPort == 123
    config.agentUnixDomainSocket == "somepath"
    config.prioritySamplingEnabled == false
    config.traceResolverEnabled == false
    config.serviceMapping == [a: "1"]
    config.mergedSpanTags == [b: "2", c: "3"]
    config.mergedJmxTags == [b: "2", d: "4", (SERVICE): config.serviceName]
    config.headerTags == [e: "5"]
    config.httpServerErrorStatuses == (122..457).toSet()
    config.httpClientErrorStatuses == (111..111).toSet()
    config.httpClientSplitByDomain == true
    config.dbClientSplitByInstance == true
    config.splitByTags == ["some.tag1", "some.tag2"].toSet()
    config.partialFlushMinSpans == 15
    config.reportHostName == true
    config.runtimeContextFieldInjection == false
    config.propagationStylesToExtract.toList() == [Config.PropagationStyle.DATADOG, Config.PropagationStyle.B3]
    config.propagationStylesToInject.toList() == [Config.PropagationStyle.B3, Config.PropagationStyle.DATADOG]
    config.jmxFetchEnabled == false
    config.jmxFetchMetricsConfigs == ["/foo.yaml", "/bar.yaml"]
    config.jmxFetchCheckPeriod == 100
    config.jmxFetchRefreshBeansPeriod == 200
    config.jmxFetchStatsdHost == "statsd host"
    config.jmxFetchStatsdPort == 321
    config.dbStatementMaxLength == 100
    config.kafkaAttemptPropagation == false
    config.recordedValueMaxLength == 10
    config.redisCaptureCommandArguments == false
    config.healthMetricsEnabled == false

    config.healthMetricsStatsdHost == "metrics statsd host"
    config.healthMetricsStatsdPort == 654
    config.traceSamplingServiceRules == [a: "1"]
    config.traceSamplingOperationRules == [b: "1"]
    config.traceSampleRate == 0.5
    config.traceRateLimit == 200

    config.profilingEnabled == true
    config.profilingUrl == "new url"
    config.mergedProfilingTags == [b: "2", f: "6", (HOST_TAG): "test-host", (SERVICE_TAG): config.serviceName, (LANGUAGE_TAG_KEY): LANGUAGE_TAG_VALUE]
    config.profilingStartDelay == 1111
    config.profilingStartForceFirst == true
    config.profilingUploadPeriod == 1112
    config.profilingUploadCompression == "off"
    config.profilingTemplateOverrideFile == "/path"
    config.profilingUploadTimeout == 1116
    config.profilingProxyHost == "proxy-host"
    config.profilingProxyPort == 1118
    config.profilingProxyUsername == "proxy-username"
    config.profilingProxyPassword == "proxy-password"
  }

  def "specify #prefix overrides via system properties"() {
    setup:
    System.setProperty(prefix + API_KEY, "new api key")
    System.setProperty(prefix + SITE, "new site")
    System.setProperty(prefix + SERVICE_NAME, "something else") // SFX
    System.setProperty(prefix + TRACE_ENABLED, "false")
    System.setProperty(prefix + WRITER_TYPE, "LoggingWriter") // SFX
    System.setProperty(prefix + USE_B3_PROPAGATION, "false") // SFX
    System.setProperty(prefix + AGENT_HOST, "somehost") // SFX
    System.setProperty(prefix + TRACE_AGENT_PORT, "123") // SFX
    System.setProperty(prefix + AGENT_UNIX_DOMAIN_SOCKET, "somepath")
    System.setProperty(prefix + AGENT_PATH, "/v2/trace") // SFX
    System.setProperty(prefix + ENDPOINT_URL, "https://example.com/") // SFX
    System.setProperty(prefix + AGENT_PORT_LEGACY, "456") // SFX
    System.setProperty(prefix + PRIORITY_SAMPLING, "false") // SFX
    System.setProperty(prefix + TRACE_RESOLVER_ENABLED, "false") // SFX
    System.setProperty(prefix + SERVICE_MAPPING, "a:1") // SFX
    System.setProperty(prefix + GLOBAL_TAGS, "b:2") // SFX
    System.setProperty(prefix + SPAN_TAGS, "c:3") // SFX
    System.setProperty(prefix + JMX_TAGS, "d:4") // SFX
    System.setProperty(prefix + HEADER_TAGS, "e:5") // SFX
    System.setProperty(prefix + HTTP_SERVER_ERROR_STATUSES, "123-456,457,124-125,122")
    System.setProperty(prefix + HTTP_CLIENT_ERROR_STATUSES, "111")
    System.setProperty(prefix + HTTP_CLIENT_HOST_SPLIT_BY_DOMAIN, "true")
    System.setProperty(prefix + DB_CLIENT_HOST_SPLIT_BY_INSTANCE, "true")
    System.setProperty(prefix + SPLIT_BY_TAGS, "some.tag3, some.tag2, some.tag1")
    System.setProperty(prefix + PARTIAL_FLUSH_MIN_SPANS, "25")
    System.setProperty(prefix + TRACE_REPORT_HOSTNAME, "true")
    System.setProperty(prefix + RUNTIME_CONTEXT_FIELD_INJECTION, "false") // SFX
    System.setProperty(prefix + PROPAGATION_STYLE_EXTRACT, "Datadog, B3")
    System.setProperty(prefix + PROPAGATION_STYLE_INJECT, "B3, Datadog")
    System.setProperty(prefix + JMX_FETCH_ENABLED, "true") // SFX
    System.setProperty(prefix + JMX_FETCH_METRICS_CONFIGS, "/foo.yaml,/bar.yaml") // SFX
    System.setProperty(prefix + JMX_FETCH_CHECK_PERIOD, "100") // SFX
    System.setProperty(prefix + JMX_FETCH_REFRESH_BEANS_PERIOD, "200") // SFX
    System.setProperty(prefix + JMX_FETCH_STATSD_HOST, "statsd host") // SFX
    System.setProperty(prefix + JMX_FETCH_STATSD_PORT, "321") // SFX
    System.setProperty(prefix + DB_STATEMENT_MAX_LENGTH, "100") // SFX
    System.setProperty(prefix + KAFKA_ATTEMPT_PROPAGATION, "false") // SFX
    System.setProperty(prefix + REDIS_CAPTURE_COMMAND_ARGUMENTS, "false") // SFX
    System.setProperty(prefix + RECORDED_VALUE_MAX_LENGTH, "100") // SFX
    System.setProperty(prefix + JMX_FETCH_ENABLED, "false")
    System.setProperty(prefix + JMX_FETCH_METRICS_CONFIGS, "/foo.yaml,/bar.yaml")
    System.setProperty(prefix + JMX_FETCH_CHECK_PERIOD, "100")
    System.setProperty(prefix + JMX_FETCH_REFRESH_BEANS_PERIOD, "200")
    System.setProperty(prefix + JMX_FETCH_STATSD_HOST, "statsd host")
    System.setProperty(prefix + JMX_FETCH_STATSD_PORT, "321")
    System.setProperty(prefix + HEALTH_METRICS_ENABLED, "true")
    System.setProperty(prefix + HEALTH_METRICS_STATSD_HOST, "metrics statsd host")
    System.setProperty(prefix + HEALTH_METRICS_STATSD_PORT, "654")
    System.setProperty(prefix + TRACE_SAMPLING_SERVICE_RULES, "a:1")
    System.setProperty(prefix + TRACE_SAMPLING_OPERATION_RULES, "b:1")
    System.setProperty(prefix + TRACE_SAMPLE_RATE, ".5")
    System.setProperty(prefix + TRACE_RATE_LIMIT, "200")

    System.setProperty(prefix + PROFILING_ENABLED, "true")
    System.setProperty(prefix + PROFILING_URL, "new url")
    System.setProperty(prefix + PROFILING_TAGS, "f:6,host:test-host")
    System.setProperty(prefix + PROFILING_START_DELAY, "1111")
    System.setProperty(prefix + PROFILING_START_FORCE_FIRST, "true")
    System.setProperty(prefix + PROFILING_UPLOAD_PERIOD, "1112")
    System.setProperty(prefix + PROFILING_TEMPLATE_OVERRIDE_FILE, "/path")
    System.setProperty(prefix + PROFILING_UPLOAD_TIMEOUT, "1116")
    System.setProperty(prefix + PROFILING_UPLOAD_COMPRESSION, "off")
    System.setProperty(prefix + PROFILING_PROXY_HOST, "proxy-host")
    System.setProperty(prefix + PROFILING_PROXY_PORT, "1118")
    System.setProperty(prefix + PROFILING_PROXY_USERNAME, "proxy-username")
    System.setProperty(prefix + PROFILING_PROXY_PASSWORD, "proxy-password")

    when:
    Config config = new Config()

    then:
    config.apiKey == null // system properties cannot be used to provide a key
    config.site == "new site"
    config.serviceName == "something else"
    config.traceEnabled == false
    config.writerType == "LoggingWriter"
    config.useB3Propagation == false
    config.getAgentHost() == "somehost"
    config.getAgentPort() == 123
    config.getAgentPath() == "/v2/trace"
    config.getAgentUseHTTPS() == true
    config.agentUnixDomainSocket == "somepath"
    config.prioritySamplingEnabled == false
    config.traceResolverEnabled == false
    config.serviceMapping == [a: "1"]
    config.mergedSpanTags == [b: "2", c: "3"]
    config.mergedJmxTags == [b: "2", d: "4", (SERVICE): config.serviceName]
    config.headerTags == [e: "5"]
    config.httpServerErrorStatuses == (122..457).toSet()
    config.httpClientErrorStatuses == (111..111).toSet()
    config.httpClientSplitByDomain == true
    config.dbClientSplitByInstance == true
    config.splitByTags == ["some.tag3", "some.tag2", "some.tag1"].toSet()
    config.partialFlushMinSpans == 25
    config.reportHostName == true
    config.runtimeContextFieldInjection == false
    config.propagationStylesToExtract.toList() == [Config.PropagationStyle.DATADOG, Config.PropagationStyle.B3]
    config.propagationStylesToInject.toList() == [Config.PropagationStyle.B3, Config.PropagationStyle.DATADOG]
    config.jmxFetchEnabled == false
    config.jmxFetchMetricsConfigs == ["/foo.yaml", "/bar.yaml"]
    config.jmxFetchCheckPeriod == 100
    config.jmxFetchRefreshBeansPeriod == 200
    config.jmxFetchStatsdHost == "statsd host"
    config.jmxFetchStatsdPort == 321

    config.healthMetricsEnabled == true
    config.healthMetricsStatsdHost == "metrics statsd host"
    config.healthMetricsStatsdPort == 654
    config.traceSamplingServiceRules == [a: "1"]
    config.traceSamplingOperationRules == [b: "1"]
    config.traceSampleRate == 0.5
    config.traceRateLimit == 200

    config.profilingEnabled == true
    config.profilingUrl == "new url"
    config.mergedProfilingTags == [b: "2", f: "6", (HOST_TAG): "test-host", (SERVICE_TAG): config.serviceName, (LANGUAGE_TAG_KEY): LANGUAGE_TAG_VALUE]
    config.profilingStartDelay == 1111
    config.profilingStartForceFirst == true
    config.profilingUploadPeriod == 1112
    config.profilingTemplateOverrideFile == "/path"
    config.profilingUploadTimeout == 1116
    config.profilingUploadCompression == "off"
    config.profilingProxyHost == "proxy-host"
    config.profilingProxyPort == 1118
    config.profilingProxyUsername == "proxy-username"
    config.profilingProxyPassword == "proxy-password"
    config.dbStatementMaxLength == 100
    config.kafkaAttemptPropagation == false
    config.redisCaptureCommandArguments == false
    config.recordedValueMaxLength == 100

    where:
    prefix      | _
    "dd."       | _
    "signalfx." | _
  }

  def "specify overrides via env vars"() {
    setup:
    environmentVariables.set(DD_API_KEY_ENV, "test-api-key")
    environmentVariables.set(DD_SERVICE_NAME_ENV, "still something else")
    environmentVariables.set(DD_TRACE_ENABLED_ENV, "false")
    environmentVariables.set(DD_WRITER_TYPE_ENV, "LoggingWriter")
    environmentVariables.set(DD_PROPAGATION_STYLE_EXTRACT, "B3 Datadog")
    environmentVariables.set(DD_PROPAGATION_STYLE_INJECT, "Datadog B3")
    environmentVariables.set(DD_JMXFETCH_METRICS_CONFIGS_ENV, "some/file")
    environmentVariables.set(DD_TRACE_REPORT_HOSTNAME, "true")
    environmentVariables.set(DD_SPAN_TAGS_ENV, "key1:value1,key2:value2")
    environmentVariables.set(SIGNALFX_DB_STATEMENT_MAX_LENGTH, "100")
    environmentVariables.set(SIGNALFX_KAFKA_ATTEMPT_PROPAGATION_ENV, "false")
    environmentVariables.set(SIGNALFX_REDIS_CAPTURE_COMMAND_ARGUMENTS, "false")
    environmentVariables.set(SIGNALFX_RECORDED_VALUE_MAX_LENGTH, "1000")

    when:
    def config = new Config()

    then:
    config.apiKey == "test-api-key"
    config.serviceName == "still something else"
    config.traceEnabled == false
    config.writerType == "LoggingWriter"
    config.propagationStylesToExtract.toList() == [Config.PropagationStyle.B3, Config.PropagationStyle.DATADOG]
    config.propagationStylesToInject.toList() == [Config.PropagationStyle.DATADOG, Config.PropagationStyle.B3]
    config.jmxFetchMetricsConfigs == ["some/file"]
    config.reportHostName == true
    config.spanTags == [key1: "value1", key2: "value2"]
    config.dbStatementMaxLength == 100
    config.kafkaAttemptPropagation == false
    config.redisCaptureCommandArguments == false
    config.recordedValueMaxLength == 1000
  }

  def "malformed endpoint url fails"() {
    setup:
    System.setProperty(PREFIX + ENDPOINT_URL, "aasdf\$!@\$%%asfkjj/aasdfsadf:")

    when:
    def config = new Config()

    then:
    config.endpointUrl == null
  }

  def "sys props override env vars"() {
    setup:
    environmentVariables.set(DD_SERVICE_NAME_ENV, "still something else")
    environmentVariables.set(DD_WRITER_TYPE_ENV, "LoggingWriter")
    environmentVariables.set(DD_TRACE_AGENT_PORT_ENV, "777")
    environmentVariables.set(SIGNALFX_DB_STATEMENT_MAX_LENGTH, "105")

    System.setProperty(PREFIX + SERVICE_NAME, "what we actually want")
    System.setProperty(PREFIX + WRITER_TYPE, "DDAgentWriter")
    System.setProperty(PREFIX + AGENT_HOST, "somewhere")
    System.setProperty(PREFIX + TRACE_AGENT_PORT, "123")
    System.setProperty(SIGNALFX_PREFIX + DB_STATEMENT_MAX_LENGTH, "1010")
    System.setProperty(SIGNALFX_PREFIX + KAFKA_ATTEMPT_PROPAGATION, "false")
    System.setProperty(SIGNALFX_PREFIX + REDIS_CAPTURE_COMMAND_ARGUMENTS, "false")

    when:
    def config = new Config()

    then:
    config.serviceName == "what we actually want"
    config.writerType == "DDAgentWriter"
    config.agentHost == "somewhere"
    config.agentPort == 123
    config.dbStatementMaxLength == 1010
    config.kafkaAttemptPropagation == false
    config.redisCaptureCommandArguments == false
  }

  def "default when configured incorrectly"() {
    setup:
    System.setProperty(PREFIX + SERVICE_NAME, " ")
    System.setProperty(PREFIX + TRACE_ENABLED, " ")
    System.setProperty(PREFIX + WRITER_TYPE, " ")
    System.setProperty(PREFIX + AGENT_HOST, " ")
    System.setProperty(PREFIX + TRACE_AGENT_PORT, " ")
    System.setProperty(PREFIX + AGENT_PORT_LEGACY, "invalid")
    System.setProperty(PREFIX + PRIORITY_SAMPLING, "3")
    System.setProperty(PREFIX + TRACE_RESOLVER_ENABLED, " ")
    System.setProperty(PREFIX + SERVICE_MAPPING, " ")
    System.setProperty(PREFIX + HEADER_TAGS, "1")
    System.setProperty(PREFIX + SPAN_TAGS, "invalid")
    System.setProperty(PREFIX + HTTP_SERVER_ERROR_STATUSES, "1111")
    System.setProperty(PREFIX + HTTP_CLIENT_ERROR_STATUSES, "1:1")
    System.setProperty(PREFIX + HTTP_CLIENT_HOST_SPLIT_BY_DOMAIN, "invalid")
    System.setProperty(PREFIX + DB_CLIENT_HOST_SPLIT_BY_INSTANCE, "invalid")
    System.setProperty(PREFIX + PROPAGATION_STYLE_EXTRACT, "some garbage")
    System.setProperty(PREFIX + PROPAGATION_STYLE_INJECT, " ")
    System.setProperty(PREFIX + DB_STATEMENT_MAX_LENGTH, "abs")
    System.setProperty(PREFIX + KAFKA_ATTEMPT_PROPAGATION, " ")
    System.setProperty(PREFIX + REDIS_CAPTURE_COMMAND_ARGUMENTS, " ")

    when:
    def config = new Config()

    then:
    config.serviceName == " "
    config.traceEnabled == true
    config.writerType == " "
    config.agentHost == " "
    config.agentPort == 9080
    config.prioritySamplingEnabled == false
    config.traceResolverEnabled == true
    config.serviceMapping == [:]
    config.mergedSpanTags == [:]
    config.headerTags == [:]
    config.httpServerErrorStatuses == (500..599).toSet()
    config.httpClientErrorStatuses == (500..599).toSet()
    config.httpClientSplitByDomain == false
    config.propagationStylesToExtract.toList() == [Config.PropagationStyle.B3]
    config.propagationStylesToInject.toList() == [Config.PropagationStyle.B3]
    config.dbStatementMaxLength == DEFAULT_DB_STATEMENT_MAX_LENGTH
    config.kafkaAttemptPropagation == DEFAULT_KAFKA_ATTEMPT_PROPAGATION
    config.redisCaptureCommandArguments == DEFAULT_REDIS_CAPTURE_COMMAND_ARGUMENTS
    config.dbClientSplitByInstance == false
    config.splitByTags == [].toSet()
  }

  def "sys props and env vars overrides for trace_agent_port and agent_port_legacy as expected"() {
    setup:
    if (overridePortEnvVar) {
      environmentVariables.set(DD_TRACE_AGENT_PORT_ENV, "777")
    }
    if (overrideLegacyPortEnvVar) {
      environmentVariables.set(DD_AGENT_PORT_LEGACY_ENV, "888")
    }

    if (overridePort) {
      System.setProperty(PREFIX + TRACE_AGENT_PORT, "123")
    }
    if (overrideLegacyPort) {
      System.setProperty(PREFIX + AGENT_PORT_LEGACY, "456")
    }

    when:
    def config = new Config()

    then:
    config.agentPort == expectedPort

    where:
    overridePort | overrideLegacyPort | overridePortEnvVar | overrideLegacyPortEnvVar | expectedPort
    true         | true               | false              | false                    | 123
    true         | false              | false              | false                    | 123
    false        | true               | false              | false                    | 456
    false        | false              | false              | false                    | 9080
    true         | true               | true               | false                    | 123
    true         | false              | true               | false                    | 123
    false        | true               | true               | false                    | 777 // env var gets picked up instead.
    false        | false              | true               | false                    | 777 // env var gets picked up instead.
    true         | true               | false              | true                     | 123
    true         | false              | false              | true                     | 123
    false        | true               | false              | true                     | 456
    false        | false              | false              | true                     | 888 // legacy env var gets picked up instead.
    true         | true               | true               | true                     | 123
    true         | false              | true               | true                     | 123
    false        | true               | true               | true                     | 777 // env var gets picked up instead.
    false        | false              | true               | true                     | 777 // env var gets picked up instead.
  }

  // FIXME: this seems to be a repeated test
  def "sys props override properties"() {
    setup:
    Properties properties = new Properties()
    properties.setProperty(SERVICE_NAME, "something else")
    properties.setProperty(TRACE_ENABLED, "false")
    properties.setProperty(WRITER_TYPE, "LoggingWriter")
    properties.setProperty(AGENT_HOST, "somehost")
    properties.setProperty(TRACE_AGENT_PORT, "123")
    properties.setProperty(AGENT_UNIX_DOMAIN_SOCKET, "somepath")
    properties.setProperty(PRIORITY_SAMPLING, "false")
    properties.setProperty(TRACE_RESOLVER_ENABLED, "false")
    properties.setProperty(SERVICE_MAPPING, "a:1")
    properties.setProperty(GLOBAL_TAGS, "b:2")
    properties.setProperty(SPAN_TAGS, "c:3")
    properties.setProperty(JMX_TAGS, "d:4")
    properties.setProperty(HEADER_TAGS, "e:5")
    properties.setProperty(HTTP_SERVER_ERROR_STATUSES, "123-456,457,124-125,122")
    properties.setProperty(HTTP_CLIENT_ERROR_STATUSES, "111")
    properties.setProperty(HTTP_CLIENT_HOST_SPLIT_BY_DOMAIN, "true")
    properties.setProperty(DB_CLIENT_HOST_SPLIT_BY_INSTANCE, "true")
    properties.setProperty(PARTIAL_FLUSH_MIN_SPANS, "15")
    properties.setProperty(PROPAGATION_STYLE_EXTRACT, "B3 Datadog")
    properties.setProperty(PROPAGATION_STYLE_INJECT, "Datadog B3")
    properties.setProperty(JMX_FETCH_METRICS_CONFIGS, "/foo.yaml,/bar.yaml")
    properties.setProperty(JMX_FETCH_CHECK_PERIOD, "100")
    properties.setProperty(JMX_FETCH_REFRESH_BEANS_PERIOD, "200")
    properties.setProperty(JMX_FETCH_STATSD_HOST, "statsd host")
    properties.setProperty(JMX_FETCH_STATSD_PORT, "321")
    properties.setProperty(DB_STATEMENT_MAX_LENGTH, "100")
    properties.setProperty(KAFKA_ATTEMPT_PROPAGATION, "false")
    properties.setProperty(REDIS_CAPTURE_COMMAND_ARGUMENTS, "false")

    when:
    def config = Config.get(properties)

    then:
    config.serviceName == "something else"
    config.traceEnabled == false
    config.writerType == "LoggingWriter"
    config.agentHost == "somehost"
    config.agentPort == 123
    config.agentUnixDomainSocket == "somepath"
    config.prioritySamplingEnabled == false
    config.traceResolverEnabled == false
    config.serviceMapping == [a: "1"]
    config.mergedSpanTags == [b: "2", c: "3"]
    config.mergedJmxTags == [b: "2", d: "4", (SERVICE): config.serviceName]
    config.headerTags == [e: "5"]
    config.httpServerErrorStatuses == (122..457).toSet()
    config.httpClientErrorStatuses == (111..111).toSet()
    config.httpClientSplitByDomain == true
    config.dbClientSplitByInstance == true
    config.splitByTags == [].toSet()
    config.partialFlushMinSpans == 15
    config.propagationStylesToExtract.toList() == [Config.PropagationStyle.B3, Config.PropagationStyle.DATADOG]
    config.propagationStylesToInject.toList() == [Config.PropagationStyle.DATADOG, Config.PropagationStyle.B3]
    config.jmxFetchMetricsConfigs == ["/foo.yaml", "/bar.yaml"]
    config.jmxFetchCheckPeriod == 100
    config.jmxFetchRefreshBeansPeriod == 200
    config.jmxFetchStatsdHost == "statsd host"
    config.jmxFetchStatsdPort == 321
    config.dbStatementMaxLength == 100
    config.kafkaAttemptPropagation == false
    config.redisCaptureCommandArguments == false
  }

  def "override null properties"() {
    when:
    def config = Config.get(null)

    then:
    config.serviceName == "unnamed-java-service"
    config.writerType == "DDAgentWriter"
    config.dbStatementMaxLength == DEFAULT_DB_STATEMENT_MAX_LENGTH
  }

  def "override empty properties"() {
    setup:
    Properties properties = new Properties()

    when:
    def config = Config.get(properties)

    then:
    config.serviceName == "unnamed-java-service"
    config.writerType == "DDAgentWriter"
    config.dbStatementMaxLength == DEFAULT_DB_STATEMENT_MAX_LENGTH
  }

  def "override non empty properties"() {
    setup:
    Properties properties = new Properties()
    properties.setProperty("foo", "bar")

    when:
    def config = Config.get(properties)

    then:
    config.serviceName == "unnamed-java-service"
    config.writerType == "DDAgentWriter"
  }

  def "verify integration config"() {
    setup:
    environmentVariables.set("DD_INTEGRATION_ORDER_ENABLED", "false")
    environmentVariables.set("DD_INTEGRATION_TEST_ENV_ENABLED", "true")
    environmentVariables.set("DD_INTEGRATION_DISABLED_ENV_ENABLED", "false")

    System.setProperty("dd.integration.order.enabled", "true")
    System.setProperty("dd.integration.test-prop.enabled", "true")
    System.setProperty("dd.integration.disabled-prop.enabled", "false")

    expect:
    Config.get().isIntegrationEnabled(integrationNames, defaultEnabled) == expected

    where:
    names                          | defaultEnabled | expected
    []                             | true           | true
    []                             | false          | false
    ["invalid"]                    | true           | true
    ["invalid"]                    | false          | false
    ["test-prop"]                  | false          | true
    ["test-env"]                   | false          | true
    ["disabled-prop"]              | true           | false
    ["disabled-env"]               | true           | false
    ["other", "test-prop"]         | false          | true
    ["other", "test-env"]          | false          | true
    ["order"]                      | false          | true
    ["test-prop", "disabled-prop"] | false          | true
    ["disabled-env", "test-env"]   | false          | true
    ["test-prop", "disabled-prop"] | true           | false
    ["disabled-env", "test-env"]   | true           | false

    integrationNames = new TreeSet<>(names)
  }

  def "verify integration jmxfetch config"() {
    setup:
    environmentVariables.set("DD_JMXFETCH_ORDER_ENABLED", "false")
    environmentVariables.set("DD_JMXFETCH_TEST_ENV_ENABLED", "true")
    environmentVariables.set("DD_JMXFETCH_DISABLED_ENV_ENABLED", "false")

    System.setProperty("dd.jmxfetch.order.enabled", "true")
    System.setProperty("dd.jmxfetch.test-prop.enabled", "true")
    System.setProperty("dd.jmxfetch.disabled-prop.enabled", "false")

    expect:
    Config.get().isJmxFetchIntegrationEnabled(integrationNames, defaultEnabled) == expected

    where:
    names                          | defaultEnabled | expected
    []                             | true           | true
    []                             | false          | false
    ["invalid"]                    | true           | true
    ["invalid"]                    | false          | false
    ["test-prop"]                  | false          | true
    ["test-env"]                   | false          | true
    ["disabled-prop"]              | true           | false
    ["disabled-env"]               | true           | false
    ["other", "test-prop"]         | false          | true
    ["other", "test-env"]          | false          | true
    ["order"]                      | false          | true
    ["test-prop", "disabled-prop"] | false          | true
    ["disabled-env", "test-env"]   | false          | true
    ["test-prop", "disabled-prop"] | true           | false
    ["disabled-env", "test-env"]   | true           | false

    integrationNames = new TreeSet<>(names)
  }

  def "verify integration trace analytics config"() {
    setup:
    environmentVariables.set("DD_ORDER_ANALYTICS_ENABLED", "false")
    environmentVariables.set("DD_TEST_ENV_ANALYTICS_ENABLED", "true")
    environmentVariables.set("DD_DISABLED_ENV_ANALYTICS_ENABLED", "false")

    System.setProperty("dd.order.analytics.enabled", "true")
    System.setProperty("dd.test-prop.analytics.enabled", "true")
    System.setProperty("dd.disabled-prop.analytics.enabled", "false")

    expect:
    Config.get().isTraceAnalyticsIntegrationEnabled(integrationNames, defaultEnabled) == expected

    where:
    names                          | defaultEnabled | expected
    []                             | true           | true
    []                             | false          | false
    ["invalid"]                    | true           | true
    ["invalid"]                    | false          | false
    ["test-prop"]                  | false          | true
    ["test-env"]                   | false          | true
    ["disabled-prop"]              | true           | false
    ["disabled-env"]               | true           | false
    ["other", "test-prop"]         | false          | true
    ["other", "test-env"]          | false          | true
    ["order"]                      | false          | true
    ["test-prop", "disabled-prop"] | false          | true
    ["disabled-env", "test-env"]   | false          | true
    ["test-prop", "disabled-prop"] | true           | false
    ["disabled-env", "test-env"]   | true           | false

    integrationNames = new TreeSet<>(names)
  }

  def "test getFloatSettingFromEnvironment(#name)"() {
    setup:
    environmentVariables.set("DD_ENV_ZERO_TEST", "0.0")
    environmentVariables.set("DD_ENV_FLOAT_TEST", "1.0")
    environmentVariables.set("DD_FLOAT_TEST", "0.2")

    System.setProperty("dd.prop.zero.test", "0")
    System.setProperty("dd.prop.float.test", "0.3")
    System.setProperty("dd.float.test", "0.4")
    System.setProperty("dd.garbage.test", "garbage")
    System.setProperty("dd.negative.test", "-1")

    expect:
    Config.getFloatSettingFromEnvironment(name, defaultValue) == (float) expected

    where:
    name              | expected
    "env.zero.test"   | 0.0
    "prop.zero.test"  | 0
    "env.float.test"  | 1.0
    "prop.float.test" | 0.3
    "float.test"      | 0.4
    "negative.test"   | -1.0
    "garbage.test"    | 10.0
    "default.test"    | 10.0

    defaultValue = 10.0
  }

  def "test getDoubleSettingFromEnvironment(#name)"() {
    setup:
    environmentVariables.set("DD_ENV_ZERO_TEST", "0.0")
    environmentVariables.set("DD_ENV_FLOAT_TEST", "1.0")
    environmentVariables.set("DD_FLOAT_TEST", "0.2")

    System.setProperty("dd.prop.zero.test", "0")
    System.setProperty("dd.prop.float.test", "0.3")
    System.setProperty("dd.float.test", "0.4")
    System.setProperty("dd.garbage.test", "garbage")
    System.setProperty("dd.negative.test", "-1")

    expect:
    Config.getDoubleSettingFromEnvironment(name, defaultValue) == (double) expected

    where:
    name              | expected
    "env.zero.test"   | 0.0
    "prop.zero.test"  | 0
    "env.float.test"  | 1.0
    "prop.float.test" | 0.3
    "float.test"      | 0.4
    "negative.test"   | -1.0
    "garbage.test"    | 10.0
    "default.test"    | 10.0

    defaultValue = 10.0
  }

  def "verify mapping configs on tracer"() {
    setup:
    System.setProperty(PREFIX + SERVICE_MAPPING, mapString)
    System.setProperty(PREFIX + SPAN_TAGS, mapString)
    System.setProperty(PREFIX + HEADER_TAGS, mapString)
    def props = new Properties()
    props.setProperty(SERVICE_MAPPING, mapString)
    props.setProperty(SPAN_TAGS, mapString)
    props.setProperty(HEADER_TAGS, mapString)

    when:
    def config = new Config()
    def propConfig = Config.get(props)

    then:
    config.serviceMapping == map
    config.spanTags == map
    config.headerTags == map
    propConfig.serviceMapping == map
    propConfig.spanTags == map
    propConfig.headerTags == map

    where:
    mapString                         | map
    "a:1, a:2, a:3"                   | [a: "3"]
    "a:b,c:d,e:"                      | [a: "b", c: "d"]
    // More different string variants:
    "a:"                              | [:]
    "a:a;"                            | [a: "a;"]
    "a:1, a:2, a:3"                   | [a: "3"]
    "a:b,c:d,e:"                      | [a: "b", c: "d"]
    "key 1!:va|ue_1,"                 | ["key 1!": "va|ue_1"]
    " key1 :value1 ,\t key2:  value2" | [key1: "value1", key2: "value2"]
    // Invalid strings:
    ""                                | [:]
    "1"                               | [:]
    "a"                               | [:]
    "a,1"                             | [:]
    "in:val:id"                       | [:]
    "a:b:c:d"                         | [:]
    "a:b,c,d"                         | [:]
    "!a"                              | [:]
  }

  def "verify integer range configs on tracer"() {
    setup:
    System.setProperty(PREFIX + HTTP_SERVER_ERROR_STATUSES, value)
    System.setProperty(PREFIX + HTTP_CLIENT_ERROR_STATUSES, value)
    def props = new Properties()
    props.setProperty(HTTP_CLIENT_ERROR_STATUSES, value)
    props.setProperty(HTTP_SERVER_ERROR_STATUSES, value)

    when:
    def config = new Config()
    def propConfig = Config.get(props)

    then:
    if (expected) {
      assert config.httpServerErrorStatuses == expected.toSet()
      assert config.httpClientErrorStatuses == expected.toSet()
      assert propConfig.httpServerErrorStatuses == expected.toSet()
      assert propConfig.httpClientErrorStatuses == expected.toSet()
    } else {
      assert config.httpServerErrorStatuses == Config.DEFAULT_HTTP_SERVER_ERROR_STATUSES
      assert config.httpClientErrorStatuses == Config.DEFAULT_HTTP_CLIENT_ERROR_STATUSES
      assert propConfig.httpServerErrorStatuses == Config.DEFAULT_HTTP_SERVER_ERROR_STATUSES
      assert propConfig.httpClientErrorStatuses == Config.DEFAULT_HTTP_CLIENT_ERROR_STATUSES
    }

    where:
    value               | expected // null means default value
    "1"                 | null
    "a"                 | null
    ""                  | null
    "1000"              | null
    "100-200-300"       | null
    "500"               | [500]
    "100,999"           | [100, 999]
    "999-888"           | 888..999
    "400-403,405-407"   | [400, 401, 402, 403, 405, 406, 407]
    " 400 - 403 , 405 " | [400, 401, 402, 403, 405]
  }

  def "verify null value mapping configs on tracer"() {
    setup:
    environmentVariables.set(DD_SERVICE_MAPPING_ENV, mapString)
    environmentVariables.set(DD_SPAN_TAGS_ENV, mapString)
    environmentVariables.set(DD_HEADER_TAGS_ENV, mapString)

    when:
    def config = new Config()

    then:
    config.serviceMapping == map
    config.spanTags == map
    config.headerTags == map

    where:
    mapString | map
    null      | [:]
    ""        | [:]
  }

  def "verify empty value list configs on tracer"() {
    setup:
    System.setProperty(PREFIX + JMX_FETCH_METRICS_CONFIGS, listString)

    when:
    def config = new Config()

    then:
    config.jmxFetchMetricsConfigs == list

    where:
    listString | list
    ""         | []
  }

  def "verify hostname not added to root span tags by default"() {
    setup:
    Properties properties = new Properties()

    when:
    def config = Config.get(properties)

    then:
    !config.localRootSpanTags.containsKey('_dd.hostname')
  }

  def "verify configuration to add hostname to root span tags"() {
    setup:
    Properties properties = new Properties()
    properties.setProperty(TRACE_REPORT_HOSTNAME, 'true')

    when:
    def config = Config.get(properties)

    then:
    config.localRootSpanTags.containsKey('_dd.hostname')
  }

  def "verify fallback to properties file"() {
    setup:
    System.setProperty(PREFIX + CONFIGURATION_FILE, "src/test/resources/dd-java-tracer.properties")

    when:
    def config = new Config()

    then:
    config.serviceName == "set-in-properties"

    cleanup:
    System.clearProperty(PREFIX + CONFIGURATION_FILE)
  }

  def "verify fallback to properties file has lower priority than system property"() {
    setup:
    System.setProperty(PREFIX + CONFIGURATION_FILE, "src/test/resources/dd-java-tracer.properties")
    System.setProperty(PREFIX + SERVICE_NAME, "set-in-system")

    when:
    def config = new Config()

    then:
    config.serviceName == "set-in-system"

    cleanup:
    System.clearProperty(PREFIX + CONFIGURATION_FILE)
    System.clearProperty(PREFIX + SERVICE_NAME)
  }

  def "verify fallback to properties file has lower priority than env var"() {
    setup:
    System.setProperty(PREFIX + CONFIGURATION_FILE, "src/test/resources/dd-java-tracer.properties")
    environmentVariables.set("DD_SERVICE_NAME", "set-in-env")
    environmentVariables.set("DD_SERVICE", "some-other-ignored-name")

    when:
    def config = new Config()

    then:
    config.serviceName == "set-in-env"

    cleanup:
    System.clearProperty(PREFIX + CONFIGURATION_FILE)
    System.clearProperty(PREFIX + SERVICE_NAME)
  }

  def "verify fallback to DD_SERVICE"() {
    setup:
    environmentVariables.set("DD_SERVICE", "service-name-from-dd-service-env-var")

    when:
    def config = new Config()

    then:
    config.serviceName == "service-name-from-dd-service-env-var"
  }

  def "verify fallback to properties file that does not exist does not crash app"() {
    setup:
    System.setProperty(PREFIX + CONFIGURATION_FILE, "src/test/resources/do-not-exist.properties")

    when:
    def config = new Config()

    then:
    config.serviceName == 'unnamed-java-service'

    cleanup:
    System.clearProperty(PREFIX + CONFIGURATION_FILE)
  }

  def "get analytics sample rate"() {
    setup:
    environmentVariables.set("DD_FOO_ANALYTICS_SAMPLE_RATE", "0.5")
    environmentVariables.set("DD_BAR_ANALYTICS_SAMPLE_RATE", "0.9")

    System.setProperty("dd.baz.analytics.sample-rate", "0.7")
    System.setProperty("dd.buzz.analytics.sample-rate", "0.3")

    when:
    String[] array = services.toArray(new String[0])
    def value = Config.get().getInstrumentationAnalyticsSampleRate(array)

    then:
    value == expected

    where:
    services                | expected
    ["foo"]                 | 0.5f
    ["baz"]                 | 0.7f
    ["doesnotexist"]        | 1.0f
    ["doesnotexist", "foo"] | 0.5f
    ["doesnotexist", "baz"] | 0.7f
    ["foo", "bar"]          | 0.5f
    ["bar", "foo"]          | 0.9f
    ["baz", "buzz"]         | 0.7f
    ["buzz", "baz"]         | 0.3f
    ["foo", "baz"]          | 0.5f
    ["baz", "foo"]          | 0.7f
  }

  def "verify api key loaded from file: #path"() {
    setup:
    environmentVariables.set(DD_API_KEY_ENV, "default-api-key")
    System.setProperty(PREFIX + API_KEY_FILE, path)

    when:
    def config = new Config()

    then:
    config.apiKey == expectedKey

    where:
    path                                                        | expectedKey
    getClass().getClassLoader().getResource("apikey").getFile() | "test-api-key"
    "/path/that/doesnt/exist"                                   | "default-api-key"
  }

  def "verify api key loaded from old option name"() {
    setup:
    environmentVariables.set(DD_PROFILING_API_KEY_OLD_ENV, "old-api-key")

    when:
    def config = new Config()

    then:
    config.apiKey == "old-api-key"
  }

  def "verify api key loaded from file for old option name: #path"() {
    setup:
    environmentVariables.set(DD_PROFILING_API_KEY_OLD_ENV, "default-api-key")
    System.setProperty(PREFIX + PROFILING_API_KEY_FILE_OLD, path)

    when:
    def config = new Config()

    then:
    config.apiKey == expectedKey

    where:
    path                                                            | expectedKey
    getClass().getClassLoader().getResource("apikey.old").getFile() | "test-api-key-old"
    "/path/that/doesnt/exist"                                       | "default-api-key"
  }

  def "verify api key loaded from very old option name"() {
    setup:
    environmentVariables.set(DD_PROFILING_API_KEY_VERY_OLD_ENV, "very-old-api-key")

    when:
    def config = new Config()

    then:
    config.apiKey == "very-old-api-key"
  }

  def "verify api key loaded from file for very old option name: #path"() {
    setup:
    environmentVariables.set(DD_PROFILING_API_KEY_VERY_OLD_ENV, "default-api-key")
    System.setProperty(PREFIX + PROFILING_API_KEY_FILE_VERY_OLD, path)

    when:
    def config = new Config()

    then:
    config.apiKey == expectedKey

    where:
    path                                                                 | expectedKey
    getClass().getClassLoader().getResource("apikey.very-old").getFile() | "test-api-key-very-old"
    "/path/that/doesnt/exist"                                            | "default-api-key"
  }

  def "verify api key loaded from new option when both new and old are set"() {
    setup:
    System.setProperty(PREFIX + API_KEY_FILE, getClass().getClassLoader().getResource("apikey").getFile())
    System.setProperty(PREFIX + PROFILING_API_KEY_FILE_OLD, getClass().getClassLoader().getResource("apikey.old").getFile())

    when:
    def config = new Config()

    then:
    config.apiKey == "test-api-key"
  }

  def "verify api key loaded from new option when both old and very old are set"() {
    setup:
    System.setProperty(PREFIX + PROFILING_API_KEY_FILE_OLD, getClass().getClassLoader().getResource("apikey.old").getFile())
    System.setProperty(PREFIX + PROFILING_API_KEY_FILE_VERY_OLD, getClass().getClassLoader().getResource("apikey.very-old").getFile())

    when:
    def config = new Config()

    then:
    config.apiKey == "test-api-key-old"
  }

  def "verify dd.tags overrides global tags in properties"() {
    setup:
    def prop = new Properties()
    prop.setProperty(TAGS, "a:1")
    prop.setProperty(GLOBAL_TAGS, "b:2")
    prop.setProperty(SPAN_TAGS, "c:3")
    prop.setProperty(JMX_TAGS, "d:4")
    prop.setProperty(HEADER_TAGS, "e:5")
    prop.setProperty(PROFILING_TAGS, "f:6")

    when:
    Config config = Config.get(prop)

    then:
    config.mergedSpanTags == [a: "1", c: "3"]
    config.mergedJmxTags == [a: "1", d: "4", (SERVICE_TAG): config.serviceName]
    config.headerTags == [e: "5"]

    config.mergedProfilingTags == [a: "1", f: "6", (HOST_TAG): config.getHostName(), (SERVICE_TAG): config.serviceName, (LANGUAGE_TAG_KEY): LANGUAGE_TAG_VALUE]
  }

  def "verify dd.tags overrides global tags in system properties"() {
    setup:
    System.setProperty(PREFIX + TAGS, "a:1")
    System.setProperty(PREFIX + GLOBAL_TAGS, "b:2")
    System.setProperty(PREFIX + SPAN_TAGS, "c:3")
    System.setProperty(PREFIX + JMX_TAGS, "d:4")
    System.setProperty(PREFIX + HEADER_TAGS, "e:5")
    System.setProperty(PREFIX + PROFILING_TAGS, "f:6")

    when:
    Config config = new Config()

    then:
    config.mergedSpanTags == [a: "1", c: "3"]
    config.mergedJmxTags == [a: "1", d: "4", (SERVICE_TAG): config.serviceName]
    config.headerTags == [e: "5"]

    config.mergedProfilingTags == [a: "1", f: "6", (HOST_TAG): config.getHostName(), (SERVICE_TAG): config.serviceName, (LANGUAGE_TAG_KEY): LANGUAGE_TAG_VALUE]
  }

  def "verify dd.tags overrides global tags in env variables"() {
    setup:
    environmentVariables.set(DD_TAGS_ENV, "a:1")
    environmentVariables.set(DD_GLOBAL_TAGS_ENV, "b:2")
    environmentVariables.set(DD_SPAN_TAGS_ENV, "c:3")
    environmentVariables.set(DD_JMX_TAGS_ENV, "d:4")
    environmentVariables.set(DD_HEADER_TAGS_ENV, "e:5")
    environmentVariables.set(DD_PROFILING_TAGS_ENV, "f:6")

    when:
    Config config = new Config()

    then:
    config.mergedSpanTags == [a: "1", c: "3"]
    config.mergedJmxTags == [a: "1", d: "4", (SERVICE_TAG): config.serviceName]
    config.headerTags == [e: "5"]

    config.mergedProfilingTags == [a: "1", f: "6", (HOST_TAG): config.getHostName(), (SERVICE_TAG): config.serviceName, (LANGUAGE_TAG_KEY): LANGUAGE_TAG_VALUE]
  }

  def "toString works when passwords are empty"() {
    when:
    def config = new Config()

    then:
    config.toString().contains("apiKey=null")
    config.toString().contains("profilingProxyPassword=null")
  }

  def "sensitive information removed for toString/debug log"() {
    setup:
    environmentVariables.set(DD_API_KEY_ENV, "test-secret-api-key")
    environmentVariables.set(DD_PROFILING_PROXY_PASSWORD_ENV, "test-secret-proxy-password")

    when:
    def config = new Config()

    then:
    config.toString().contains("apiKey=****")
    !config.toString().contains("test-secret-api-key")
    config.toString().contains("profilingProxyPassword=****")
    !config.toString().contains("test-secret-proxy-password")
    config.apiKey == "test-secret-api-key"
    config.profilingProxyPassword == "test-secret-proxy-password"
  }

  def "custom datadog site"() {
    setup:
    def prop = new Properties()
    prop.setProperty(SITE, "some.new.site")

    when:
    Config config = Config.get(prop)

    then:
    config.getFinalProfilingUrl() == "https://intake.profile.some.new.site/v1/input"
  }


  def "custom profiling url override"() {
    setup:
    def prop = new Properties()
    prop.setProperty(SITE, "some.new.site")
    prop.setProperty(PROFILING_URL, "https://some.new.url/goes/here")

    when:
    Config config = Config.get(prop)

    then:
    config.getFinalProfilingUrl() == "https://some.new.url/goes/here"
  }

  def "fallback to DD_TAGS"() {
    setup:
    environmentVariables.set(DD_TAGS_ENV, "a:1,b:2,c:3")

    when:
    Config config = new Config()

    then:
    config.mergedSpanTags == [a: "1", c: "3", b: "2"]
  }

  def "precedence of DD_ENV and DD_VERSION"() {
    setup:
    environmentVariables.set(DD_ENV_ENV, "test_env")
    environmentVariables.set(DD_VERSION_ENV, "1.2.3")
    environmentVariables.set(DD_TAGS_ENV, "signalfx.env:production   ,    signalfx.version:3.2.1")

    when:
    Config config = new Config()

    then:
    config.mergedSpanTags == ["signalfx.env": "test_env", "signalfx.version": "1.2.3"]
  }

  def "propertyNameToEnvironmentVariableName unit test"() {
    expect:
    Config.propertyNameToEnvironmentVariableName(Config.SERVICE) == "SIGNALFX_SERVICE"
  }

  def "getProperty*Value unit test"() {
    setup:
    def p = new Properties()
    p.setProperty("a", "42.42")
    p.setProperty("intProp", "13")

    expect:
    Config.getPropertyDoubleValue(p, "intProp", 40) == 13
    Config.getPropertyDoubleValue(p, "a", 41) == 42.42
    Config.getPropertyIntegerValue(p, "b", 61) == 61
    Config.getPropertyIntegerValue(p, "intProp", 61) == 13
    Config.getPropertyBooleanValue(p, "a", true) == false
  }

  def "valueOf positive test"() {
    expect:
    Config.valueOf(value, tClass, defaultValue) == expected

    where:
    value      | tClass  | defaultValue | expected
    "42.42"    | Boolean | true         | false
    "42.42"    | Boolean | null         | false
    "true"     | Boolean | null         | true
    "trUe"     | Boolean | null         | true
    "trUe"     | Boolean | false        | true
    "tru"      | Boolean | true         | false
    "truee"    | Boolean | true         | false
    "true "    | Boolean | true         | false
    " true"    | Boolean | true         | false
    " true "   | Boolean | true         | false
    "   true  "| Boolean | true         | false
    null       | Float   | 43.3         | 43.3
    "42.42"    | Float   | 21.21        | 42.42f
    null       | Double  | 43.3         | 43.3
    "42.42"    | Double  | 21.21        | 42.42
    null       | Integer | 13           | 13
    "44"       | Integer | 21           | 44
    "45"       | Long    | 21           | 45
    "46"       | Short   | 21           | 46
  }

  def "valueOf negative test when tClass is null"() {
    when:
    Config.valueOf(value, tClass, defaultValue)

    then:
    def exception = thrown(NullPointerException)
    exception.message == "tClass is marked non-null but is null"

    where:
    value      | tClass  | defaultValue
    null       | null    | "42"
    ""         | null    | "43"
    "      "   | null    | "44"
    "1"        | null    | "45"
  }

  def "valueOf negative test"() {
    when:
    Config.valueOf(value, tClass, null)

    then:
    def exception = thrown(NumberFormatException)
    println("cause: " : exception.message)

    where:
    value      | tClass
    "42.42"    | Number
    "42.42"    | Byte
    "42.42"    | Character
    "42.42"    | Short
    "42.42"    | Integer
    "42.42"    | Long
    "42.42"    | Object
    "42.42"    | Object[]
    "42.42"    | boolean[]
    "42.42"    | boolean
    "42.42"    | byte
    "42.42"    | byte
    "42.42"    | char
    "42.42"    | short
    "42.42"    | int
    "42.42"    | long
    "42.42"    | double
    "42.42"    | float
  }

}
