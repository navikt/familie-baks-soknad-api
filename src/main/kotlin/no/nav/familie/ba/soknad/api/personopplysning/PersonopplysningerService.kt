package no.nav.familie.ba.soknad.api.personopplysning

import org.springframework.stereotype.Service


@Service
class PersonopplysningerService(private val pdlClient: PdlClient) {

    fun hentPersoninfo(ident: String): Person {
        return pdlClient.hentPerson(ident)
    }

}