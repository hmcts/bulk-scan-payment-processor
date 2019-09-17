provider "azurerm" {
  version = "1.22.1"
}

locals {
  ase_name   = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  is_preview = "${(var.env == "preview" || var.env == "spreview")}"
  local_env  = "${local.is_preview ? "aat" : var.env}"
  sku_size   = "${var.env == "prod" || var.env == "sprod" || var.env == "aat" ? "I2" : "I1"}"
}

module "bulk-scan-pay-processor" {
  source                          = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product                         = "${var.product}-${var.component}"
  location                        = "${var.location}"
  env                             = "${var.env}"
  ilbIp                           = "${var.ilbIp}"
  subscription                    = "${var.subscription}"
  is_frontend                     = "false"
  capacity                        = "${var.capacity}"
  common_tags                     = "${var.common_tags}"
  appinsights_instrumentation_key = "${var.appinsights_instrumentation_key}"
  instance_size                   = "${local.sku_size}"
  asp_name                        = "${var.product}-${var.env}"
  asp_rg                          = "${var.product}-${var.env}"
  java_container_version          = "9.0"

  app_settings = {}
}
