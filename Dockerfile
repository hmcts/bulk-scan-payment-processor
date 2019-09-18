ARG APP_INSIGHTS_AGENT_VERSION=2.3.1

FROM hmcts/cnp-java-base:openjdk-11-distroless-1.0-beta

COPY build/libs/bulk-scan-payment-processor.jar /opt/app/
COPY lib/applicationinsights-agent-2.4.0-BETA-SNAPSHOT.jar lib/AI-Agent.xml /opt/app/

EXPOSE 8583

CMD ["bulk-scan-payment-processor.jar"]
