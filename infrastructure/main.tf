provider "azurerm" {
  version = "=1.22.1"
}

locals {
  is_preview          = "${(var.env == "preview" || var.env == "spreview")}"
  local_env           = "${local.is_preview ? "aat" : var.env}"

  s2s_rg              = "rpe-service-auth-provider-${local.local_env}"
  s2s_url             = "http://${local.s2s_rg}.service.core-compute-${local.local_env}.internal"

  vaultName           = "bulk-scan-${var.env}"
}

core_app_settings = {
  S2S_SECRET = "${data.azurerm_key_vault_secret.s2s_secret.value}"
}


data "azurerm_key_vault" "s2s_key_vault" {
  name                = "s2s-${local.local_env}"
  resource_group_name = "${local.s2s_rg}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  key_vault_id = "${data.azurerm_key_vault.s2s_key_vault.id}"
  name      = "microservicekey-bulk-scan-payment-processor"
}

# Copy s2s key from shared to local vault
data "azurerm_key_vault" "local_key_vault" {
  name = "${local.vaultName}"
  resource_group_name = "${local.vaultName}"
}

resource "azurerm_key_vault_secret" "local_s2s_key" {
  name         = "microservicekey-bulk-scan-payment-processor"
  value        = "${data.azurerm_key_vault_secret.s2s_secret.value}"
  key_vault_id = "${data.azurerm_key_vault.local_key_vault.id}"
}
