ARG APP_INSIGHTS_AGENT_VERSION=3.4.8

# Application image
FROM hmctspublic.azurecr.io/base/java:21-distroless

COPY lib/applicationinsights.json /opt/app/

COPY build/libs/bulk-scan-payment-processor.jar /opt/app/

EXPOSE 8583

CMD ["bulk-scan-payment-processor.jar"]
