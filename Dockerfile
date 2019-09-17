ARG APP_INSIGHTS_AGENT_VERSION=2.3.1

FROM hmctspublic.azurecr.io/base/java:openjdk-8-distroless-1.0

COPY build/libs/bulk-scan-payment-processor.jar /opt/app/
COPY lib/applicationinsights-agent-2.3.1.jar lib/AI-Agent.xml /opt/app/

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD http_proxy="" wget -q --spider http://localhost:8583/health || exit 1

EXPOSE 8583

CMD ["bulk-scan-payment-processor.jar"]
