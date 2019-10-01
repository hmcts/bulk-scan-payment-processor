variable "raw_product" {
  default = "bulk-scan" // jenkins-library overrides product for PRs and adds e.g. pr-118-bulk-scan
}

variable "env" {
  type = "string"
}

variable "s2s_name" {
  default = "bulk_scan_payment_processor"
}
