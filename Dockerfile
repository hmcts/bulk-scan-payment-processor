ARG APP_INSIGHTS_AGENT_VERSION=3.2.6

# Build image

FROM busybox:1 as downloader

RUN wget -P /tmp https://github.com/microsoft/ApplicationInsights-Java/releases/download/2.5.1/applicationinsights-agent-2.5.1.jar

# Application image

FROM hmctspublic.azurecr.io/base/java:17-distroless

COPY --from=downloader /tmp/applicationinsights-agent-${APP_INSIGHTS_AGENT_VERSION}.jar /opt/app/

COPY lib/applicationinsights.json /opt/app/

COPY build/libs/bulk-scan-payment-processor.jar /opt/app/

EXPOSE 8583

CMD ["bulk-scan-payment-processor.jar"]
