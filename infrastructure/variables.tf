
variable "raw_product" {
  default = "bulk-scan" // jenkins-library overrides product for PRs and adds e.g. pr-118-bulk-scan
}

variable "component" {}

variable "location" {
  type    = "string"
  default = "UK South"
}

variable "location_app" {
  type    = "string"
  default = "UK South"
}

variable "env" {
  type = "string"
}

variable "ilbIp" {}

variable "subscription" {}

variable "capacity" {
  default = "1"
}

variable "common_tags" {
  type = "map"
}

variable "appinsights_instrumentation_key" {
  description = "Instrumentation key of the App Insights instance this webapp should use. Module will create own App Insights resource if this is not provided"
  default     = ""
}
