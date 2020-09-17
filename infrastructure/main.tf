provider "azurerm" {
  features {}
}

# Make sure the resource group exists
resource "azurerm_resource_group" "rg" {
  name     = "${var.product}-${var.component}-${var.env}"
  location = "${var.location_app}"
}

data "azurerm_key_vault" "s2s_key_vault" {
  name                = "s2s-${var.env}"
  resource_group_name = "rpe-service-auth-provider-${var.env}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  key_vault_id = "${data.azurerm_key_vault.s2s_key_vault.id}"
  name         = "microservicekey-bulk-scan-payment-processor"
}

data "azurerm_key_vault_secret" "idam_client_secret" {
  key_vault_id = "${data.azurerm_key_vault.bulk_scan_key_vault.id}"
  name         = "idam-client-secret"
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
