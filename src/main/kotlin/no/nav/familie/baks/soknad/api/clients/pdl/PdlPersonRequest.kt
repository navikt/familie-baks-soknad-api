package no.nav.familie.baks.soknad.api.clients.pdl

data class PdlPersonRequest(val variables: PdlPersonRequestVariables, val query: String)

data class PdlPersonRequestVariables(var ident: String)
