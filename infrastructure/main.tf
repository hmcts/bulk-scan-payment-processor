provider "azurerm" {
  features {}
}

locals {
  s2s_rg  = "rpe-service-auth-provider-${var.env}"
  s2s_url = "http://${local.s2s_rg}.service.core-compute-${var.env}.internal"

  // configures a user for a service
  // add secrets to all bulk-scan vaults in the form idam-users-<service>-username idam-users-<service>-password
  users = {
    SSCS     = "idam-users-sscs"
    BULKSCAN = "idam-users-bulkscan"
    DIVORCE  = "idam-users-div"
    PROBATE  = "idam-users-probate"
    FINREM   = "idam-users-finrem"
    CMC      = "idam-users-cmc"
  }

  # maps the names of environment variables representing PayHub site IDs to key vault secret names
  payhub_sites = {
    SITE_ID_PROBATE = "site-id-probate"
    SITE_ID_DIVORCE = "site-id-divorce"
    SITE_ID_FINREM  = "site-id-finrem"
    # site-id-bulkscan secret should not be defined in prod
    SITE_ID_BULKSCAN = "site-id-bulkscan"
  }

  all_services          = "${keys(local.users)}"
  supported_user_keys   = "${matchkeys(local.all_services, local.all_services, var.supported_services)}"
  supported_user_values = "${matchkeys(values(local.users), local.all_services, var.supported_services)}"

  # a subset of local.users, limited to the supported services
  supported_users = "${zipmap(local.supported_user_keys, local.supported_user_values)}"

  users_secret_names = "${values(local.supported_users)}"

  users_usernames_settings = "${zipmap(
                                    formatlist("IDAM_USERS_%s_USERNAME", keys(local.supported_users)),
                                    data.azurerm_key_vault_secret.idam_users_usernames.*.value
                                )}"

  users_passwords_settings = "${zipmap(
                                    formatlist("IDAM_USERS_%s_PASSWORD", keys(local.supported_users)),
                                    data.azurerm_key_vault_secret.idam_users_passwords.*.value
                                )}"

  payhub_site_id_secret_names = "${values(local.payhub_sites)}"

  payhub_site_settings = "${zipmap(
                                    keys(local.payhub_sites),
                                    data.azurerm_key_vault_secret.payhub_site_ids.*.value
                                )}"

  core_app_settings = {
    PAY_HUB_URL                           = "http://ccpay-bulkscanning-api-${var.env}.service.core-compute-${var.env}.internal"
    S2S_URL                               = "${local.s2s_url}"
    S2S_SECRET                            = "${data.azurerm_key_vault_secret.s2s_secret.value}"
    IDAM_API_URL                          = "https://idam-api.${var.env}.platform.hmcts.net"
    IDAM_CLIENT_REDIRECT_URI              = "${var.idam_client_redirect_uri}"
    CORE_CASE_DATA_API_URL                = "http://ccd-data-store-api-${var.env}.service.core-compute-${var.env}.internal"
    IDAM_CLIENT_SECRET                    = "${data.azurerm_key_vault_secret.idam_client_secret.value}"
    PAYMENTS_QUEUE_MAX_DELIVERY_COUNT     = "5"
  }
}

module "bulk-scan-orchestrator" {
  source                          = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product                         = "${var.product}-${var.component}"
  location                        = "${var.location_app}"
  env                             = "${var.env}"
  ilbIp                           = "${var.ilbIp}"
  resource_group_name             = "${var.product}-${var.component}-${var.env}"
  subscription                    = "${var.subscription}"
  capacity                        = "${var.capacity}"
  common_tags                     = "${var.common_tags}"
  appinsights_instrumentation_key = data.azurerm_key_vault_secret.appinsights_secret
  asp_name                        = "${var.product}-${var.env}"
  asp_rg                          = "${var.product}-${var.env}"
  instance_size                   = "I1"
  java_version                    = "11"

  app_settings = "${merge(local.core_app_settings, local.users_usernames_settings, local.users_passwords_settings, local.payhub_site_settings)}"
  enable_ase   = "${var.enable_ase}"
}

data "azurerm_key_vault" "s2s_key_vault" {
  name                = "s2s-${var.env}"
  resource_group_name = "rpe-service-auth-provider-${var.env}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  key_vault_id = data.azurerm_key_vault.s2s_key_vault.id
  name         = "microservicekey-bulk-scan-payment-processor"
}

data "azurerm_key_vault_secret" "idam_client_secret" {
  key_vault_id = data.azurerm_key_vault.bulk_scan_key_vault.id
  name         = "idam-client-secret"
}

# Copy s2s secret from s2s key vault to bulkscan key vault
data "azurerm_key_vault" "bulk_scan_key_vault" {
  name                = "bulk-scan-${var.env}"
  resource_group_name = "bulk-scan-${var.env}"
}

resource "azurerm_key_vault_secret" "bulk_scan_s2s_secret" {
  name         = "s2s-secret-payment-processor"
  value        = data.azurerm_key_vault_secret.s2s_secret.value
  key_vault_id = data.azurerm_key_vault.bulk_scan_key_vault.id
}

data "azurerm_key_vault_secret" "idam_users_usernames" {
  count        = "${length(local.users_secret_names)}"
  key_vault_id = "${data.azurerm_key_vault.bulk_scan_key_vault.id}"
  name         = "${local.users_secret_names[count.index]}-username"
}

data "azurerm_key_vault_secret" "idam_users_passwords" {
  count        = "${length(local.users_secret_names)}"
  key_vault_id = "${data.azurerm_key_vault.bulk_scan_key_vault.id}"
  name         = "${local.users_secret_names[count.index]}-password"
}

data "azurerm_key_vault_secret" "payhub_site_ids" {
  count        = "${length(local.payhub_sites)}"
  key_vault_id = "${data.azurerm_key_vault.bulk_scan_key_vault.id}"
  name         = "${local.payhub_site_id_secret_names[count.index]}"
}

data "azurerm_key_vault_secret" "appinsights_secret" {
  key_vault_id = data.azurerm_key_vault.bulk_scan_key_vault.id
  name = "app-insights-instrumentation-key"
}
