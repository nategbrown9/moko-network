/*
 * Copyright 2019 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

package org.sagebionetworks.bridge.mpp

import dev.icerock.moko.mvvm.livedata.LiveData
import dev.icerock.moko.mvvm.livedata.MutableLiveData
import dev.icerock.moko.mvvm.livedata.readOnly
import dev.icerock.moko.mvvm.viewmodel.ViewModel
import dev.icerock.moko.network.LanguageProvider
import dev.icerock.moko.network.features.LanguageFeature
import dev.icerock.moko.network.features.TokenFeature
import io.ktor.client.HttpClient
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.sagebionetworks.bridge.mpp.network.generated.apis.AssessmentsApi
import org.sagebionetworks.bridge.mpp.network.generated.apis.AuthenticationApi
import org.sagebionetworks.bridge.mpp.network.generated.apis.PublicApi
import org.sagebionetworks.bridge.mpp.network.generated.models.Assessment
import org.sagebionetworks.bridge.mpp.network.generated.models.AssessmentConfig
import org.sagebionetworks.bridge.mpp.network.generated.models.SignIn

class TestViewModel : ViewModel() {

    private val sessionTokenProvider = object : TokenFeature.TokenProvider {

        var sessionToken: String? = null

        override fun getToken(): String? {
            return sessionToken
        }
    }

    private val httpClient = HttpClient {

        install(LanguageFeature) {
            languageHeaderName = "X-Language"
            languageCodeProvider = LanguageProvider()
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = object: Logger {
                override fun log(message: String) {
                    println(message)
                }
            }
        }
        install(TokenFeature) {
            tokenHeaderName = "Bridge-Session"
            tokenProvider = sessionTokenProvider
        }
    }
    private val publicApi = PublicApi(
        httpClient = httpClient,
        json = Json.nonstrict
    )

    private val assessmentsApi = AssessmentsApi(
        httpClient = httpClient,
        json = Json.nonstrict
    )

    private val authApi = AuthenticationApi(
        httpClient = httpClient,
        json = Json.nonstrict
    )

    private val _petInfo = MutableLiveData<String?>(null)
    val petInfo: LiveData<String?> = _petInfo.readOnly()

    init {
        reloadPet()
    }

    fun onRefreshPressed() {
        reloadPet()
    }

    private fun reloadPet() {
        viewModelScope.launch {
            try {

                val signIn = SignIn("an app id", "an email", password = "a password" )
                val userSession = authApi.signIn(signIn)
                sessionTokenProvider.sessionToken = userSession.sessionToken

                val assessment = Assessment(
                    identifier = "Assessment_Test_1",
                    revision = 1,
                    ownerId = "sage",
                    title = "Assessment Test 1",
                    osName = "Android")

                //val aResult = assessmentsApi.createAssessment(assessment)
                //val guid = aResult.guid

                val configElement = Json.parseJson(configString)

                val config = AssessmentConfig(
                    config = configElement,
                    version = 1
                )
                //assessmentsApi.updateAssessmentConfig(guid, config)




                val a = assessmentsApi.getAssessmentConfig("guid")//assessmentsApi.getAssessments(0, 50, null, null)


                //val appConfig = publicApi.getAppConfigForStudy("sage-assessment-test")
                //val pet = petApi.findPetsByStatus(listOf("available"))

                _petInfo.value = a.toString()
            } catch (error: Exception) {
                println("can't load $error")
            }
        }
    }

    private val configString = "{\n" +
            "  \"type\": \"assessment\",\n" +
            "  \"identifier\": \"foo\",\n" +
            "  \"versionString\": \"1.2.3\",\n" +
            "  \"resultIdentifier\":\"bar\",\n" +
            "  \"title\": \"Hello World!\",\n" +
            "  \"subtitle\": \"Subtitle\",\n" +
            "  \"detail\": \"Some text. This is a test.\",\n" +
            "  \"estimatedMinutes\": 4,\n" +
            "  \"icon\": \"fooIcon\",\n" +
            "  \"footnote\": \"This is a footnote.\",\n" +
            "  \"shouldHideActions\": [\"goBackward\"],\n" +
            "  \"progressMarkers\": [\"step1\",\"step2\"],\n" +
            "  \"steps\": [\n" +
            "    {\n" +
            "      \"identifier\": \"step1\",\n" +
            "      \"type\": \"instruction\",\n" +
            "      \"title\": \"Instruction Step 1\",\n" +
            "      \"detail\": \"Here are the details for this instruction.\",\n" +
            "      \"image\"  : {\n" +
            "        \"type\":\"fetchable\",\n" +
            "      \"identifier\": \"instructionImage\",\n" +
            "        \"imageName\" : \"crf_seated\"\n" +
            "      }\n" +
            "    }\n" +

            "  ]\n" +
            "}"
}