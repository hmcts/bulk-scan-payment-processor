provider "azurerm" {
  version = "=1.22.1"
}

locals {
  s2s_rg              = "rpe-service-auth-provider-${var.env}"
  s2s_url             = "http://${local.s2s_rg}.service.core-compute-${var.env}.internal"
}

data "azurerm_key_vault" "s2s_key_vault" {
  name                = "s2s-${var.env}"
  resource_group_name = "${local.s2s_rg}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  key_vault_id = "${data.azurerm_key_vault.s2s_key_vault.id}"
  name         = "microservicekey-bulk-scan-payment-processor"
}

# Copy s2s secret from s2s key vault to bulkscan key vault
data "azurerm_key_vault" "bulk_scan_key_vault" {
  name                = "bulk-scan-${var.env}"
  resource_group_name = "bulk-scan-${var.env}"
}

resource "azurerm_key_vault_secret" "bulk_scan_s2s_secret" {
  name         = "s2s-secret-payment-processor"
  value        = "${data.azurerm_key_vault_secret.s2s_secret.value}"
  key_vault_id = "${data.azurerm_key_vault.bulk_scan_key_vault.id}"
}
