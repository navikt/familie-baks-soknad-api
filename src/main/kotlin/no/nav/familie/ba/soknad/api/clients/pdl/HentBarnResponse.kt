package no.nav.familie.ba.soknad.api.clients.pdl

import no.nav.familie.kontrakter.felles.personopplysning.Bostedsadresse

data class HentBarnResponse(val navn: String, val fødselsdato: String, val adresse: Bostedsadresse?)
