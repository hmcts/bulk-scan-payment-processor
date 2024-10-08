variable "env" {}

variable "product" {}

variable "raw_product" {
  default = "bulk-scan"
}

variable "component" {}

variable "location_app" {
  default = "UK South"
}

variable "tenant_id" {}

variable "jenkins_AAD_objectId" {
  description = "(Required) The Azure AD object ID of a user, service principal or security group in the Azure Active Directory tenant for the vault. The object ID must be unique for the list of access policies."
}

variable "subscription" {}

variable "capacity" {
  default = "1"
}

variable "common_tags" {
  type = map(string)
}

variable "appinsights_instrumentation_key" {
  description = "Instrumentation key of the App Insights instance this webapp should use. Module will create own App Insights resource if this is not provided"
  default     = ""
}

variable "idam_client_redirect_uri" {
  default = "https://bulk-scan-orchestrator-sandbox.service.core-compute-sandbox.internal/oauth2/callback"
}

variable "supported_services" {
  type        = list(any)
  description = "Services to be supported by Bulk Scan in the given environment. Bulk Scan will only be able to map these ones to IDAM user credentials"
  default     = ["SSCS", "BULKSCAN", "PROBATE", "DIVORCE", "FINREM", "CMC", "PRIVATELAW"]
}
